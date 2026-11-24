import { useState } from "react";
import useApi from "../hooks/useApi.js";
import { api, MATCH_STATUSES } from "../services/mockApi.js";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

const winRate = (p) => (p.matchesPlayed ? Math.round((p.matchesWon / p.matchesPlayed) * 100) : 0);

const METRICS = {
  net: { label: "Net wins", value: (p) => p.matchesWon - p.matchesLost, fmt: (v) => (v > 0 ? `+${v}` : v) },
  wins: { label: "Wins", value: (p) => p.matchesWon, fmt: (v) => v },
  points: { label: "Points", value: (p) => p.pointsScored, fmt: (v) => v },
  rate: { label: "Win rate", value: winRate, fmt: (v) => `${v}%` },
};

const COLUMNS = [
  { key: "name", label: "Player" },
  { key: "matchesPlayed", label: "Played" },
  { key: "matchesWon", label: "Won" },
  { key: "matchesLost", label: "Lost" },
  { key: "matchesDrawn", label: "Drawn" },
  { key: "rate", label: "Win %" },
  { key: "pointsScored", label: "Points" },
];

const STATUS_COLORS = { SCHEDULED: "var(--brand-2)", COMPLETED: "var(--green)", CANCELLED: "#b8bfcc" };

function Analytics() {
  const { data, loading, error, reload } = useApi(() => api.getAnalytics(), []);
  const { data: players, error: playersError } = useApi(() => api.getPlayers(), []);
  const { data: matches } = useApi(() => api.getMatches(), []);
  const [metric, setMetric] = useState("net");
  const [sort, setSort] = useState({ key: "matchesWon", dir: "desc" });
  const [onlyRegistered, setOnlyRegistered] = useState(false);

  if (loading) return <Loading />;
  if (error)
    return (
      <>
        <ErrorBox message={error} />
        <button className="btn btn--ghost" style={{ alignSelf: "flex-start" }} onClick={reload}>
          Retry
        </button>
      </>
    );

  const maxSport = Math.max(1, ...data.bySport.map((s) => s.players));
  const pool = (players || []).filter((p) => !onlyRegistered || p.registered);

  const m = METRICS[metric];
  const leaderboard = [...pool]
    .sort((a, b) => m.value(b) - m.value(a) || b.pointsScored - a.pointsScored)
    .slice(0, 5);

  const sortVal = (p, k) => (k === "rate" ? winRate(p) : p[k]);
  const table = [...pool].sort((a, b) => {
    const va = sortVal(a, sort.key);
    const vb = sortVal(b, sort.key);
    const c = typeof va === "string" ? va.localeCompare(vb) : va - vb;
    return sort.dir === "asc" ? c : -c;
  });

  const totals = pool.reduce(
    (acc, p) => ({
      won: acc.won + p.matchesWon,
      lost: acc.lost + p.matchesLost,
      drawn: acc.drawn + p.matchesDrawn,
    }),
    { won: 0, lost: 0, drawn: 0 },
  );

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Analytics</h2>
          <p>Participation, performance and leaderboard across all sports.</p>
        </div>
        <button className="btn btn--ghost" onClick={reload}>
          ↻ Refresh
        </button>
      </div>

      <div className="kpi-grid">
        {data.kpis.map((k) => (
          <div className="card kpi" key={k.label}>
            <span className="kpi-icon" aria-hidden="true">
              {k.icon}
            </span>
            <div>
              <div className="kpi-value">{k.value}</div>
              <div className="stat-label">{k.label}</div>
            </div>
          </div>
        ))}
      </div>

      <div className="grid-2">
        <div className="card card-pad">
          <h3>Sport-wise participation</h3>
          <p style={{ margin: "-6px 0 14px", color: "var(--text-2)", fontSize: 12.5 }}>
            Players listing each sport as a preference
          </p>
          {data.bySport.map((s) => (
            <div className="bar-row" key={s.sport}>
              <span>{s.sport}</span>
              <div style={{ background: "var(--brand-soft)", borderRadius: 999 }}>
                <div
                  className="bar"
                  style={{ width: `${(s.players / maxSport) * 100}%`, minWidth: s.players ? 10 : 0 }}
                  role="img"
                  aria-label={`${s.sport}: ${s.players} players`}
                />
              </div>
              <span className="mono">{s.players}</span>
            </div>
          ))}
        </div>

        <div className="card card-pad">
          <h3>Match status</h3>
          {!matches ? (
            <Loading />
          ) : !matches.length ? (
            <Empty icon="⚽" text="No matches recorded yet." />
          ) : (
            <Donut matches={matches} />
          )}
        </div>
      </div>

      <div className="grid-2">
        <div className="card card-pad">
          <div style={{ display: "flex", justifyContent: "space-between", gap: 10, flexWrap: "wrap", alignItems: "center", marginBottom: 6 }}>
            <h3 style={{ margin: 0 }}>🏆 Leaderboard</h3>
            <select className="select" style={{ maxWidth: 200 }} value={metric} onChange={(e) => setMetric(e.target.value)} aria-label="Rank by">
              {Object.entries(METRICS).map(([k, v]) => (
                <option key={k} value={k}>
                  Rank by {v.label.toLowerCase()}
                </option>
              ))}
            </select>
          </div>
          {leaderboard.map((p, i) => (
            <div className="rank" key={p.id}>
              <span className="rank-num">{i + 1}</span>
              <span style={{ flex: 1 }}>
                {i === 0 && "🥇 "}
                {i === 1 && "🥈 "}
                {i === 2 && "🥉 "}
                {p.name}
              </span>
              <span className="mono">{m.fmt(m.value(p))}</span>
            </div>
          ))}
          {!leaderboard.length && <Empty icon="🏆" text="No players to rank." />}
        </div>

        <div className="card card-pad">
          <h3>Overall results</h3>
          {pool.length ? (
            <>
              <StackBar won={totals.won} lost={totals.lost} drawn={totals.drawn} />
              <div className="legend">
                <span style={{ color: "var(--green)" }}>Won {totals.won}</span>
                <span style={{ color: "var(--red)" }}>Lost {totals.lost}</span>
                <span style={{ color: "var(--amber)" }}>Drawn {totals.drawn}</span>
              </div>
            </>
          ) : (
            <Empty text="No player results yet." />
          )}
        </div>
      </div>

      <div className="card card-pad">
        <div style={{ display: "flex", justifyContent: "space-between", gap: 10, flexWrap: "wrap", alignItems: "center", marginBottom: 12 }}>
          <h3 style={{ margin: 0 }}>Player statistics</h3>
          <div className="checks">
            <label>
              <input type="checkbox" checked={onlyRegistered} onChange={(e) => setOnlyRegistered(e.target.checked)} /> Registered players only
            </label>
          </div>
        </div>
        {playersError && <ErrorBox message={playersError} />}
        {!players && !playersError && <Loading />}
        {players && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  {COLUMNS.map((c) => (
                    <th key={c.key} aria-sort={sort.key === c.key ? (sort.dir === "asc" ? "ascending" : "descending") : undefined}>
                      <button
                        className="btn btn--ghost btn--sm"
                        style={{ border: 0, padding: 0, background: "none", font: "inherit", color: "inherit" }}
                        onClick={() =>
                          setSort((s) => ({ key: c.key, dir: s.key === c.key && s.dir === "desc" ? "asc" : "desc" }))
                        }
                      >
                        {c.label} {sort.key === c.key ? (sort.dir === "asc" ? "▲" : "▼") : ""}
                      </button>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {table.map((p) => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td className="mono">{p.matchesPlayed}</td>
                    <td className="mono">{p.matchesWon}</td>
                    <td className="mono">{p.matchesLost}</td>
                    <td className="mono">{p.matchesDrawn}</td>
                    <td>
                      <div style={{ display: "flex", alignItems: "center", gap: 8, minWidth: 110 }}>
                        <div style={{ flex: 1, background: "var(--brand-soft)", borderRadius: 999 }}>
                          <div className="bar" style={{ width: `${winRate(p)}%` }} />
                        </div>
                        <span className="mono">{winRate(p)}%</span>
                      </div>
                    </td>
                    <td className="mono">{p.pointsScored}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!table.length && <Empty text="No players match this filter." />}
          </div>
        )}
      </div>
    </>
  );
}

function StackBar({ won, lost, drawn }) {
  const total = won + lost + drawn || 1;
  const seg = (v, color) => <div style={{ width: `${(v / total) * 100}%`, background: color }} />;
  return (
    <div
      role="img"
      aria-label={`Won ${won}, lost ${lost}, drawn ${drawn}`}
      style={{ display: "flex", height: 18, borderRadius: 999, overflow: "hidden", gap: 2, background: "var(--border)" }}
    >
      {seg(won, "var(--green)")}
      {seg(lost, "var(--red)")}
      {seg(drawn, "var(--amber)")}
    </div>
  );
}

function Donut({ matches }) {
  const counts = MATCH_STATUSES.map((s) => ({ s, n: matches.filter((m) => m.status === s).length }));
  const total = matches.length;
  const R = 54;
  const C = 2 * Math.PI * R;
  let offset = 0;
  return (
    <div style={{ display: "flex", gap: 24, alignItems: "center", flexWrap: "wrap" }}>
      <svg width="140" height="140" viewBox="0 0 140 140" role="img" aria-label={counts.map((c) => `${c.s} ${c.n}`).join(", ")}>
        <circle cx="70" cy="70" r={R} fill="none" stroke="var(--border)" strokeWidth="18" />
        {counts.map(({ s, n }) => {
          const len = (n / total) * C;
          const el = (
            <circle
              key={s}
              cx="70"
              cy="70"
              r={R}
              fill="none"
              stroke={STATUS_COLORS[s]}
              strokeWidth="18"
              strokeDasharray={`${len} ${C - len}`}
              strokeDashoffset={-offset}
              transform="rotate(-90 70 70)"
            />
          );
          offset += len;
          return el;
        })}
        <text x="70" y="68" textAnchor="middle" fontSize="24" fontWeight="800" fill="var(--text)">
          {total}
        </text>
        <text x="70" y="88" textAnchor="middle" fontSize="11" fill="var(--text-2)">
          matches
        </text>
      </svg>
      <ul style={{ listStyle: "none", padding: 0, margin: 0, fontSize: 13.5 }}>
        {counts.map(({ s, n }) => (
          <li key={s} style={{ display: "flex", alignItems: "center", gap: 8, padding: "4px 0" }}>
            <span style={{ width: 10, height: 10, borderRadius: 3, background: STATUS_COLORS[s] }} />
            {s.charAt(0) + s.slice(1).toLowerCase()}
            <strong style={{ marginLeft: 6 }}>{n}</strong>
          </li>
        ))}
      </ul>
    </div>
  );
}

Analytics.title = "Analytics";
Analytics.icon = "📈";
export default Analytics;
