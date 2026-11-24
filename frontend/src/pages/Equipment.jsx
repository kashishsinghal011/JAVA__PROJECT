import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SPORTS, CONDITIONS } from "../services/mockApi.js";
import Toasts from "../components/Toasts.jsx";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

const conditionClass = (c) =>
  ({
    EXCELLENT: "badge--green",
    GOOD: "badge--blue",
    WORN: "badge--amber",
    NEEDS_REPAIR: "badge--red",
    RETIRED: "badge--gray",
  })[c] || "badge--gray";
const pretty = (s) =>
  s
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/^\w/, (c) => c.toUpperCase());

const availability = (e) => {
  if (e.availableUnits === 0)
    return { label: "Out of stock", cls: "badge--red" };
  if (e.availableUnits / e.totalUnits <= 0.25)
    return { label: "Low stock", cls: "badge--amber" };
  return { label: "Available", cls: "badge--green" };
};

const EMPTY_FORM = {
  name: "",
  sport: SPORTS[0],
  condition: "GOOD",
  totalUnits: "",
};

function Equipment() {
  const {
    data: equipment,
    loading,
    error,
    reload,
  } = useApi(() => api.getEquipment(), []);
  const {
    data: issued,
    error: issuedError,
    reload: reloadIssued,
  } = useApi(() => api.getIssued(), []);
  const { data: players } = useApi(() => api.getPlayers(), []);
  const { toasts, push } = useToasts();

  const [query, setQuery] = useState("");
  const [sportFilter, setSportFilter] = useState("ALL");
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [formError, setFormError] = useState(null);
  const [issueFor, setIssueFor] = useState(null);
  const [issuePlayer, setIssuePlayer] = useState("");
  const [issueError, setIssueError] = useState(null);
  const [confirmDelete, setConfirmDelete] = useState(null);
  const [busy, setBusy] = useState(false);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const refresh = () => {
    reload();
    reloadIssued();
  };

  const visible = equipment.filter(
    (e) =>
      (sportFilter === "ALL" || e.sport === sportFilter) &&
      [e.name, e.id, e.sport].join(" ").toLowerCase().includes(query.toLowerCase()),
  );

  const totals = equipment.reduce(
    (acc, e) => ({
      total: acc.total + e.totalUnits,
      available: acc.available + e.availableUnits,
      issued: acc.issued + e.issuedUnits,
      attention:
        acc.attention + (["NEEDS_REPAIR", "WORN"].includes(e.condition) ? 1 : 0),
    }),
    { total: 0, available: 0, issued: 0, attention: 0 },
  );

  const submitItem = async (payload) => {
    try {
      if (editing) {
        await api.updateEquipment(editing.id, payload);
        push("Equipment updated");
      } else {
        await api.addEquipment(payload);
        push("Equipment added");
      }
      setFormError(null);
      setShowForm(false);
      setEditing(null);
      refresh();
    } catch (err) {
      setFormError(err.message);
    }
  };

  const issue = async (e) => {
    e.preventDefault();
    setBusy(true);
    try {
      await api.issueEquipment(issueFor.id, issuePlayer);
      push(`${issueFor.name} issued`);
      setIssueFor(null);
      setIssueError(null);
      refresh();
    } catch (err) {
      setIssueError(err.message);
    } finally {
      setBusy(false);
    }
  };

  const doReturn = async (i) => {
    try {
      await api.returnEquipment(i.id);
      push(`${i.item} returned`);
      refresh();
    } catch (err) {
      push(err.message, "error");
    }
  };

  const doDelete = async () => {
    setBusy(true);
    try {
      await api.deleteEquipment(confirmDelete.id);
      push(`${confirmDelete.name} deleted`);
      setConfirmDelete(null);
      refresh();
    } catch (err) {
      push(err.message, "error");
      setConfirmDelete(null);
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Equipment Inventory</h2>
          <p>Stock levels, condition, and issue / return tracking.</p>
        </div>
        <button
          className="btn"
          onClick={() => {
            setEditing(null);
            setFormError(null);
            setShowForm(true);
          }}
        >
          + Add equipment
        </button>
      </div>

      <div className="stat-grid">
        <Stat icon="📦" tone="blue" value={totals.total} label="Total units" sub={`${equipment.length} item types`} />
        <Stat icon="✅" tone="green" value={totals.available} label="Available" sub="ready to issue" />
        <Stat icon="🎽" tone="violet" value={totals.issued} label="Issued" sub={`${issued?.length ?? 0} active issue records`} />
        <Stat icon="🛠️" tone="amber" value={totals.attention} label="Need attention" sub="worn or needs repair" />
      </div>

      {(showForm || editing) && (
        <EquipmentForm
          key={editing?.id || "new"}
          initial={editing}
          error={formError}
          onSubmit={submitItem}
          onCancel={() => {
            setShowForm(false);
            setEditing(null);
            setFormError(null);
          }}
        />
      )}

      {confirmDelete && (
        <div className="card card-pad" role="alertdialog" aria-labelledby="del-title">
          <h3 id="del-title">Delete {confirmDelete.name}?</h3>
          <p style={{ margin: 0, color: "var(--text-2)", fontSize: 13.5 }}>
            This removes all {confirmDelete.totalUnits} units of this item from
            the inventory. Items with units still on issue cannot be deleted.
          </p>
          <div className="form-actions">
            <button className="btn btn--danger" onClick={doDelete} disabled={busy}>
              {busy ? "Deleting…" : "Yes, delete"}
            </button>
            <button className="btn btn--ghost" onClick={() => setConfirmDelete(null)}>
              Keep it
            </button>
          </div>
        </div>
      )}

      {issueFor && (
        <form className="card card-pad" onSubmit={issue}>
          <h3>
            Issue {issueFor.name}{" "}
            <span className="badge badge--blue" style={{ marginLeft: 6 }}>
              {issueFor.availableUnits} available
            </span>
          </h3>
          <div className="form-grid">
            <div className="field">
              <label htmlFor="eq-player">Issue to player</label>
              <select
                id="eq-player"
                className="select"
                required
                value={issuePlayer}
                onChange={(e) => setIssuePlayer(e.target.value)}
              >
                <option value="">Select player…</option>
                {(players || []).map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name} ({p.rollNo})
                  </option>
                ))}
              </select>
            </div>
            <button className="btn" type="submit" disabled={!issuePlayer || busy}>
              {busy ? "Issuing…" : "Issue item"}
            </button>
            <button className="btn btn--ghost" type="button" onClick={() => setIssueFor(null)}>
              Cancel
            </button>
          </div>
          {issueError && (
            <div className="form-error" role="alert">
              {issueError}
            </div>
          )}
        </form>
      )}

      <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
        <input
          className="input search"
          placeholder="Search equipment…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          aria-label="Search equipment"
        />
        <select
          className="select"
          style={{ maxWidth: 200 }}
          value={sportFilter}
          onChange={(e) => setSportFilter(e.target.value)}
          aria-label="Filter by sport"
        >
          <option value="ALL">All sports</option>
          {SPORTS.map((s) => (
            <option key={s}>{s}</option>
          ))}
        </select>
      </div>

      <div className="card table-card table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Item</th>
              <th>Sport</th>
              <th>Condition</th>
              <th>Available / Total</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {visible.map((e) => {
              const a = availability(e);
              return (
                <tr key={e.id}>
                  <td className="mono">{e.id}</td>
                  <td>{e.name}</td>
                  <td>{e.sport}</td>
                  <td>
                    <span className={`badge ${conditionClass(e.condition)}`}>
                      {pretty(e.condition)}
                    </span>
                  </td>
                  <td className="mono">
                    {e.availableUnits} / {e.totalUnits}
                  </td>
                  <td>
                    <span className={`badge ${a.cls}`}>{a.label}</span>
                  </td>
                  <td>
                    <div className="actions">
                      <button
                        className="btn btn--sm"
                        disabled={e.availableUnits === 0}
                        onClick={() => {
                          setIssueFor(e);
                          setIssuePlayer("");
                          setIssueError(null);
                        }}
                      >
                        Issue
                      </button>
                      <button
                        className="btn btn--ghost btn--sm"
                        onClick={() => {
                          setShowForm(false);
                          setFormError(null);
                          setEditing(e);
                        }}
                      >
                        Edit
                      </button>
                      <button
                        className="btn btn--danger btn--sm"
                        onClick={() => setConfirmDelete(e)}
                        aria-label={`Delete ${e.name}`}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {!visible.length && <Empty icon="🎽" text="No equipment matches your filters." />}
      </div>

      <div className="card card-pad">
        <h3>Currently issued ({issued?.length ?? 0})</h3>
        {issuedError && <ErrorBox message={issuedError} />}
        {!issued && !issuedError && <Loading />}
        {issued && issued.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Issue ID</th>
                  <th>Item</th>
                  <th>Player</th>
                  <th>Issued on</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {issued.map((i) => (
                  <tr key={i.id}>
                    <td className="mono">{i.id}</td>
                    <td>{i.item}</td>
                    <td>{i.playerName}</td>
                    <td className="mono">{i.issuedOn}</td>
                    <td>
                      <button className="btn btn--ghost btn--sm" onClick={() => doReturn(i)}>
                        ↩ Return
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {issued && !issued.length && <Empty icon="📦" text="Nothing is on issue right now." />}
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}

function Stat({ icon, tone, value, label, sub }) {
  return (
    <div className="card stat">
      <div className={`stat-icon ${tone}`}>{icon}</div>
      <div>
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
        <div className="stat-sub">{sub}</div>
      </div>
    </div>
  );
}

function EquipmentForm({ initial, error, onSubmit, onCancel }) {
  const [form, setForm] = useState(
    initial
      ? {
          name: initial.name,
          sport: initial.sport,
          condition: initial.condition,
          totalUnits: initial.totalUnits,
        }
      : EMPTY_FORM,
  );
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  return (
    <form
      className="card card-pad"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({ ...form, totalUnits: Number(form.totalUnits) });
      }}
    >
      <h3>{initial ? `Edit ${initial.name}` : "New equipment"}</h3>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="eq-name">Item name</label>
          <input id="eq-name" className="input" required value={form.name} onChange={(e) => set("name", e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="eq-sport">Sport</label>
          <select id="eq-sport" className="select" value={form.sport} onChange={(e) => set("sport", e.target.value)}>
            {SPORTS.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="eq-cond">Condition</label>
          <select id="eq-cond" className="select" value={form.condition} onChange={(e) => set("condition", e.target.value)}>
            {CONDITIONS.map((c) => (
              <option key={c} value={c}>
                {pretty(c)}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="eq-units">Total units</label>
          <input
            id="eq-units"
            className="input"
            type="number"
            min="1"
            required
            value={form.totalUnits}
            onChange={(e) => set("totalUnits", e.target.value)}
          />
        </div>
      </div>
      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}
      <div className="form-actions">
        <button className="btn" type="submit">
          {initial ? "Save changes" : "Add equipment"}
        </button>
        <button className="btn btn--ghost" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

Equipment.title = "Equipment Inventory";
Equipment.icon = "🎽";
export default Equipment;
