import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api } from "../services/mockApi.js";
import Toasts from "../components/Toasts.jsx";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

const COUNT_LABELS = {
  players: "Players",
  coaches: "Coaches",
  teams: "Teams",
  tournaments: "Tournaments",
  bookings: "Bookings",
  equipment: "Equipment items",
  issued: "Items on issue",
  matches: "Matches",
  payments: "Payments",
};

const fmtDate = (iso) =>
  new Date(iso).toLocaleString("en-IN", { day: "numeric", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" });

function DataControls() {
  const { data: snapshots, loading, error, reload } = useApi(() => api.listSnapshots(), []);
  const { data: counts, reload: reloadCounts } = useApi(() => api.getDataCounts(), []);
  const { toasts, push } = useToasts();
  const [label, setLabel] = useState("");
  const [busy, setBusy] = useState(null);
  // { type: 'restore' | 'delete' | 'reset', snap? }
  const [confirm, setConfirm] = useState(null);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const refresh = () => {
    reload();
    reloadCounts();
  };

  const save = async (e) => {
    e.preventDefault();
    setBusy("save");
    try {
      const { persisted } = await api.saveSnapshot(label);
      push(persisted ? "Snapshot saved in this browser" : "Snapshot saved for this session only (browser storage unavailable)", persisted ? "success" : "error");
      setLabel("");
      refresh();
    } catch (err) {
      push(err.message, "error");
    } finally {
      setBusy(null);
    }
  };

  const runConfirmed = async () => {
    const { type, snap } = confirm;
    setBusy(type);
    try {
      if (type === "restore") {
        await api.restoreSnapshot(snap.id);
        push(`Restored "${snap.label}"`);
      } else if (type === "delete") {
        await api.deleteSnapshot(snap.id);
        push(`Deleted "${snap.label}"`);
      } else if (type === "reset") {
        await api.resetData();
        push("Sample data restored");
      }
      refresh();
    } catch (err) {
      push(err.message, "error");
    } finally {
      setBusy(null);
      setConfirm(null);
    }
  };

  const confirmText = confirm && {
    restore: {
      title: `Restore "${confirm.snap?.label}"?`,
      body: "All current players, teams, tournaments, bookings, equipment, matches and payments will be replaced with this snapshot. Unsaved changes will be lost — save a snapshot first if you want to keep them.",
      action: "Yes, restore",
    },
    delete: {
      title: `Delete "${confirm.snap?.label}"?`,
      body: "This snapshot will be removed permanently from this browser. Current data is not affected.",
      action: "Yes, delete",
    },
    reset: {
      title: "Reset to the original sample data?",
      body: "Every change made in this preview will be discarded and the built-in sample data reloaded. Saved snapshots are kept.",
      action: "Yes, reset",
    },
  }[confirm.type];

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Save / Load Data</h2>
          <p>Save the current preview data as a snapshot and restore it later.</p>
        </div>
      </div>

      <div className="preview-banner" role="note" style={{ margin: 0 }}>
        Snapshots are stored in <strong>this browser only</strong> (localStorage). They are not
        written to the Java backend's data file and are not shared with other devices.
      </div>

      <div className="grid-2">
        <form className="card card-pad" onSubmit={save}>
          <h3>💾 Save snapshot</h3>
          <div className="field">
            <label htmlFor="snap-label">Snapshot name (optional)</label>
            <input
              id="snap-label"
              className="input"
              placeholder="e.g. Before inter-hostel cup"
              maxLength={60}
              value={label}
              onChange={(e) => setLabel(e.target.value)}
            />
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={!!busy}>
              {busy === "save" ? "Saving…" : "Save current data"}
            </button>
            <button className="btn btn--danger" type="button" disabled={!!busy} onClick={() => setConfirm({ type: "reset" })}>
              Reset to sample data
            </button>
          </div>
        </form>

        <div className="card card-pad">
          <h3>📊 Current data</h3>
          {!counts ? (
            <Loading />
          ) : (
            <div className="checks">
              {Object.entries(COUNT_LABELS).map(([k, l]) => (
                <span key={k} className="badge badge--blue">
                  {l}: {counts[k]}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>

      {confirm && (
        <div className="card card-pad" role="alertdialog" aria-labelledby="dc-title" style={{ borderColor: "#f3c6c6" }}>
          <h3 id="dc-title">⚠️ {confirmText.title}</h3>
          <p style={{ margin: 0, color: "var(--text-2)", fontSize: 13.5 }}>{confirmText.body}</p>
          <div className="form-actions">
            <button className="btn btn--danger" onClick={runConfirmed} disabled={!!busy}>
              {busy ? "Working…" : confirmText.action}
            </button>
            <button className="btn btn--ghost" onClick={() => setConfirm(null)} disabled={!!busy}>
              Cancel
            </button>
          </div>
        </div>
      )}

      <div className="card card-pad">
        <h3>📂 Saved snapshots ({snapshots.length})</h3>
        {snapshots.length ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Saved</th>
                  <th>Contents</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {snapshots.map((s) => (
                  <tr key={s.id}>
                    <td>
                      {s.label}
                      <div className="mono" style={{ fontSize: 11.5, color: "var(--text-2)" }}>
                        {s.id}
                      </div>
                    </td>
                    <td className="mono">{fmtDate(s.createdAt)}</td>
                    <td style={{ fontSize: 12.5, color: "var(--text-2)" }}>
                      {s.counts.players} players · {s.counts.teams} teams · {s.counts.tournaments} tournaments ·{" "}
                      {s.counts.matches} matches
                    </td>
                    <td>
                      <div className="actions">
                        <button className="btn btn--sm" disabled={!!busy} onClick={() => setConfirm({ type: "restore", snap: s })}>
                          Restore
                        </button>
                        <button className="btn btn--danger btn--sm" disabled={!!busy} onClick={() => setConfirm({ type: "delete", snap: s })}>
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <Empty icon="💾" text="No snapshots yet — save one above." />
        )}
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}

DataControls.title = "Save / Load Data";
DataControls.icon = "💾";
export default DataControls;
