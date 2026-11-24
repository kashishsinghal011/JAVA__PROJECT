import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SPORTS, SLOTS, MATCH_STATUSES } from "../services/mockApi.js";
import Toasts from "../components/Toasts.jsx";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

const statusClass = (s) =>
  ({ SCHEDULED: "badge--blue", COMPLETED: "badge--green", CANCELLED: "badge--gray" })[s];
const pretty = (s) => s.toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
const today = () => new Date().toISOString().slice(0, 10);

const TABS = [
  { key: "UPCOMING", label: "Upcoming" },
  { key: "COMPLETED", label: "Completed" },
  { key: "CANCELLED", label: "Cancelled" },
  { key: "ALL", label: "All" },
];

function Matches() {
  const {
    data: matches,
    loading,
    error,
    reload,
  } = useApi(() => api.getMatches(), []);
  const { data: teams } = useApi(() => api.getTeams(), []);
  const { data: players } = useApi(() => api.getPlayers(), []);
  const { data: tournaments } = useApi(() => api.getTournaments(), []);
  const { toasts, push } = useToasts();

  const [tab, setTab] = useState("UPCOMING");
  const [sportFilter, setSportFilter] = useState("ALL");
  const [showForm, setShowForm] = useState(false);
  const [scoring, setScoring] = useState(null);
  const [confirmCancel, setConfirmCancel] = useState(null);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const sorted = [...matches].sort((a, b) =>
    tab === "COMPLETED" ? b.date.localeCompare(a.date) : a.date.localeCompare(b.date),
  );
  const visible = sorted.filter(
    (m) =>
      (sportFilter === "ALL" || m.sport === sportFilter) &&
      (tab === "ALL" ||
        (tab === "UPCOMING" ? m.status === "SCHEDULED" : m.status === tab)),
  );
  const counts = Object.fromEntries(
    MATCH_STATUSES.map((s) => [s, matches.filter((m) => m.status === s).length]),
  );

  const cancel = async () => {
    try {
      await api.cancelMatch(confirmCancel.id);
      push("Match cancelled");
      reload();
    } catch (err) {
      push(err.message, "error");
    }
    setConfirmCancel(null);
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Matches &amp; Scores</h2>
          <p>
            {counts.SCHEDULED} upcoming · {counts.COMPLETED} completed ·{" "}
            {counts.CANCELLED} cancelled
          </p>
        </div>
        <button className="btn" onClick={() => setShowForm((s) => !s)}>
          {showForm ? "Close form" : "+ Schedule match"}
        </button>
      </div>

      {showForm && (
        <ScheduleForm
          teams={teams || []}
          players={players || []}
          tournaments={tournaments || []}
          onCancel={() => setShowForm(false)}
          onDone={() => {
            push("Match scheduled");
            setShowForm(false);
            setTab("UPCOMING");
            reload();
          }}
        />
      )}

      {confirmCancel && (
        <div className="card card-pad" role="alertdialog" aria-labelledby="mc-title">
          <h3 id="mc-title">
            Cancel {confirmCancel.home} vs {confirmCancel.away}?
          </h3>
          <p style={{ margin: 0, color: "var(--text-2)", fontSize: 13.5 }}>
            The fixture on {confirmCancel.date} ({confirmCancel.slot}) will be
            marked cancelled and can no longer be scored.
          </p>
          <div className="form-actions">
            <button className="btn btn--danger" onClick={cancel}>
              Yes, cancel match
            </button>
            <button className="btn btn--ghost" onClick={() => setConfirmCancel(null)}>
              Keep it
            </button>
          </div>
        </div>
      )}

      <div style={{ display: "flex", gap: 10, flexWrap: "wrap", alignItems: "center" }}>
        <div className="checks" role="tablist" aria-label="Match status">
          {TABS.map((t) => (
            <label key={t.key}>
              <input
                type="radio"
                name="mtab"
                checked={tab === t.key}
                onChange={() => setTab(t.key)}
              />{" "}
              {t.label}
            </label>
          ))}
        </div>
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

      <div className="grid-cards">
        {visible.map((m) => (
          <div className="card card-pad" key={m.id}>
            <div
              style={{
                display: "flex",
                justifyContent: "space-between",
                gap: 8,
                alignItems: "flex-start",
              }}
            >
              <span className="mono" style={{ fontSize: 12, color: "var(--text-2)" }}>
                {m.id} · {m.sport}
              </span>
              <span className={`badge ${statusClass(m.status)}`}>{pretty(m.status)}</span>
            </div>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "1fr auto 1fr",
                gap: 10,
                alignItems: "center",
                margin: "14px 0",
                textAlign: "center",
              }}
            >
              <Side name={m.home} score={m.homeScore} win={m.status === "COMPLETED" && m.homeScore > m.awayScore} />
              <span style={{ color: "var(--text-2)", fontWeight: 700 }}>
                {m.status === "COMPLETED" ? "–" : "vs"}
              </span>
              <Side name={m.away} score={m.awayScore} win={m.status === "COMPLETED" && m.awayScore > m.homeScore} />
            </div>
            <p style={{ color: "var(--text-2)", fontSize: 13, margin: "0 0 12px" }}>
              {m.tournament || "Friendly"} · {m.date} · {m.slot}
            </p>

            {scoring?.id === m.id ? (
              <ScoreForm
                match={m}
                onCancel={() => setScoring(null)}
                onDone={() => {
                  push("Result recorded");
                  setScoring(null);
                  reload();
                }}
              />
            ) : (
              m.status !== "CANCELLED" && (
                <div className="actions">
                  <button className="btn btn--sm" onClick={() => setScoring(m)}>
                    {m.status === "COMPLETED" ? "Edit score" : "Record score"}
                  </button>
                  {m.status === "SCHEDULED" && (
                    <button className="btn btn--danger btn--sm" onClick={() => setConfirmCancel(m)}>
                      Cancel match
                    </button>
                  )}
                </div>
              )
            )}
          </div>
        ))}
        {!visible.length && (
          <div className="card" style={{ gridColumn: "1/-1" }}>
            <Empty icon="⚽" text="No matches in this view." />
          </div>
        )}
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}

function Side({ name, score, win }) {
  return (
    <div>
      <div style={{ fontWeight: win ? 800 : 600, fontSize: 14 }}>
        {win && <span aria-label="winner">🏅 </span>}
        {name}
      </div>
      {score !== null && score !== undefined && (
        <div className="kpi-value" style={{ marginTop: 4 }}>
          {score}
        </div>
      )}
    </div>
  );
}

function ScoreForm({ match, onDone, onCancel }) {
  const [home, setHome] = useState(match.homeScore ?? "");
  const [away, setAway] = useState(match.awayScore ?? "");
  const [err, setErr] = useState(null);
  const [busy, setBusy] = useState(false);
  return (
    <form
      onSubmit={async (e) => {
        e.preventDefault();
        setBusy(true);
        try {
          await api.recordScore(match.id, Number(home), Number(away));
          onDone();
        } catch (ex) {
          setErr(ex.message);
          setBusy(false);
        }
      }}
    >
      <div className="form-grid" style={{ gridTemplateColumns: "1fr 1fr" }}>
        <div className="field">
          <label htmlFor={`hs-${match.id}`}>{match.home}</label>
          <input id={`hs-${match.id}`} className="input" type="number" min="0" required value={home} onChange={(e) => setHome(e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor={`as-${match.id}`}>{match.away}</label>
          <input id={`as-${match.id}`} className="input" type="number" min="0" required value={away} onChange={(e) => setAway(e.target.value)} />
        </div>
      </div>
      {err && (
        <div className="form-error" role="alert">
          {err}
        </div>
      )}
      <div className="form-actions">
        <button className="btn btn--sm" type="submit" disabled={busy}>
          {busy ? "Saving…" : "Save result"}
        </button>
        <button className="btn btn--ghost btn--sm" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

function ScheduleForm({ teams, players, tournaments, onDone, onCancel }) {
  const [form, setForm] = useState({
    tournament: "",
    sport: SPORTS[0],
    home: "",
    away: "",
    date: today(),
    slot: SLOTS[0],
  });
  const [err, setErr] = useState(null);
  const [busy, setBusy] = useState(false);
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const sportTeams = teams.filter((t) => t.sport === form.sport);
  const sportPlayers = players.filter((p) => p.preferences.includes(form.sport));

  const pickTournament = (name) => {
    const t = tournaments.find((x) => x.name === name);
    setForm((f) => ({ ...f, tournament: name, sport: t ? t.sport : f.sport, home: "", away: "" }));
  };

  const participantSelect = (key, label) => (
    <div className="field">
      <label htmlFor={`ms-${key}`}>{label}</label>
      <select id={`ms-${key}`} className="select" required value={form[key]} onChange={(e) => set(key, e.target.value)}>
        <option value="">Select…</option>
        {sportTeams.length > 0 && (
          <optgroup label="Teams">
            {sportTeams.map((t) => (
              <option key={t.id} value={t.name}>
                {t.name}
              </option>
            ))}
          </optgroup>
        )}
        {sportPlayers.length > 0 && (
          <optgroup label="Players">
            {sportPlayers.map((p) => (
              <option key={p.id} value={p.name}>
                {p.name}
              </option>
            ))}
          </optgroup>
        )}
      </select>
    </div>
  );

  return (
    <form
      className="card card-pad"
      onSubmit={async (e) => {
        e.preventDefault();
        if (form.date < today()) {
          setErr("Matches cannot be scheduled in the past");
          return;
        }
        setBusy(true);
        try {
          await api.scheduleMatch({ ...form, tournament: form.tournament || "Friendly" });
          onDone();
        } catch (ex) {
          setErr(ex.message);
          setBusy(false);
        }
      }}
    >
      <h3>Schedule a match</h3>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="ms-tour">Tournament</label>
          <select id="ms-tour" className="select" value={form.tournament} onChange={(e) => pickTournament(e.target.value)}>
            <option value="">Friendly (no tournament)</option>
            {tournaments.map((t) => (
              <option key={t.id} value={t.name}>
                {t.name}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="ms-sport">Sport</label>
          <select
            id="ms-sport"
            className="select"
            value={form.sport}
            disabled={!!form.tournament}
            onChange={(e) => setForm((f) => ({ ...f, sport: e.target.value, home: "", away: "" }))}
          >
            {SPORTS.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </div>
        {participantSelect("home", "Home side")}
        {participantSelect("away", "Away side")}
        <div className="field">
          <label htmlFor="ms-date">Date</label>
          <input id="ms-date" className="input" type="date" min={today()} required value={form.date} onChange={(e) => set("date", e.target.value)} />
        </div>
        <div className="field">
          <label htmlFor="ms-slot">Time slot</label>
          <select id="ms-slot" className="select" value={form.slot} onChange={(e) => set("slot", e.target.value)}>
            {SLOTS.map((s) => (
              <option key={s}>{s}</option>
            ))}
          </select>
        </div>
      </div>
      {!sportTeams.length && !sportPlayers.length && (
        <p className="field-error" style={{ marginTop: 10 }}>
          No teams or players are linked to {form.sport} yet.
        </p>
      )}
      {err && (
        <div className="form-error" role="alert">
          {err}
        </div>
      )}
      <div className="form-actions">
        <button className="btn" type="submit" disabled={busy}>
          {busy ? "Scheduling…" : "Schedule match"}
        </button>
        <button className="btn btn--ghost" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

Matches.title = "Match Scheduling & Scores";
Matches.icon = "⚽";
export default Matches;
