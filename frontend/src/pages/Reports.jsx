import { useState } from "react";
import useApi from "../hooks/useApi.js";
import { api, SPORTS, TOURNAMENT_STATUSES } from "../services/mockApi.js";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

const inr = (n) => `₹${Number(n).toLocaleString("en-IN")}`;
const pretty = (s) =>
  s
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/^\w/, (c) => c.toUpperCase());
const payClass = (s) => ({ PAID: "badge--green", SUCCESS: "badge--green", PENDING: "badge--amber", FAILED: "badge--red" })[s] || "badge--gray";
const tourClass = (s) =>
  ({
    DRAFT: "badge--gray",
    REGISTRATION_OPEN: "badge--green",
    REGISTRATION_CLOSED: "badge--amber",
    IN_PROGRESS: "badge--violet",
    COMPLETED: "badge--blue",
  })[s];

/** Escape a value for CSV output. */
const csvCell = (v) => `"${String(v ?? "").replaceAll('"', '""')}"`;

function Reports() {
  const { data: reports, loading, error, reload } = useApi(() => api.getReports(), []);
  const { data: payments, error: payError } = useApi(() => api.getPayments(), []);
  const { data: players } = useApi(() => api.getPlayers(), []);
  const { data: tournaments } = useApi(() => api.getTournaments(), []);
  const { data: equipment } = useApi(() => api.getEquipment(), []);
  const { data: schedule } = useApi(() => api.getSchedule(new Date().toISOString().slice(0, 10)), []);

  const [active, setActive] = useState("ALL");
  const [payStatus, setPayStatus] = useState("ALL");
  const [payQuery, setPayQuery] = useState("");
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [sportFilter, setSportFilter] = useState("ALL");

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

  const show = (tag) => active === "ALL" || active === tag;

  // ---- live headline figure shown on each report card
  const headline = {
    Players: players ? `${players.filter((p) => p.registered).length}/${players.length} registered` : "…",
    Tournaments: tournaments ? `${tournaments.filter((t) => t.status === "REGISTRATION_OPEN").length} open for entries` : "…",
    Facilities: schedule
      ? `${schedule.rows.reduce((s, r) => s + r.slots.filter((x) => x.booking).length, 0)} slot(s) booked today`
      : "…",
    Equipment: equipment ? `${equipment.filter((e) => ["WORN", "NEEDS_REPAIR"].includes(e.condition)).length} items need attention` : "…",
    Payments: payments ? `${inr(payments.filter((p) => p.status === "PENDING").reduce((s, p) => s + p.amount, 0))} outstanding` : "…",
  };

  const payStatuses = payments ? [...new Set(payments.map((p) => p.status))] : [];
  const filteredPayments = (payments || []).filter(
    (p) =>
      (payStatus === "ALL" || p.status === payStatus) &&
      (!from || p.date >= from) &&
      (!to || p.date <= to) &&
      [p.id, p.player, p.description].join(" ").toLowerCase().includes(payQuery.toLowerCase()),
  );
  const sum = (list, status) => list.filter((p) => !status || p.status === status).reduce((s, p) => s + p.amount, 0);

  const exportCsv = () => {
    const header = ["Payment ID", "Player", "Description", "Amount (INR)", "Status", "Date"];
    const rows = filteredPayments.map((p) => [p.id, p.player, p.description, p.amount, p.status, p.date]);
    const csv = [header, ...rows].map((r) => r.map(csvCell).join(",")).join("\n");
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
    const a = document.createElement("a");
    a.href = url;
    a.download = `sportssphere-payments-${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(url);
  };

  const visibleTournaments = (tournaments || []).filter((t) => sportFilter === "ALL" || t.sport === sportFilter);

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Reports</h2>
          <p>Live summaries of registrations, tournaments, facilities, equipment and payments.</p>
        </div>
        {active !== "ALL" && (
          <button className="btn btn--ghost" onClick={() => setActive("ALL")}>
            Show all reports
          </button>
        )}
      </div>

      <div className="grid-cards">
        {reports.map((r) => (
          <button
            key={r.title}
            className="card tile"
            onClick={() => setActive((a) => (a === r.tag ? "ALL" : r.tag))}
            aria-pressed={active === r.tag}
            style={{
              textAlign: "left",
              cursor: "pointer",
              font: "inherit",
              color: "inherit",
              outline: active === r.tag ? "2px solid var(--brand-2)" : undefined,
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <span className="tile-icon" aria-hidden="true">
                {r.icon}
              </span>
              <span className="badge badge--blue">{r.tag}</span>
            </div>
            <h4>{r.title}</h4>
            <p>{r.desc}</p>
            <p style={{ marginTop: 10, color: "var(--brand)", fontWeight: 700 }}>{headline[r.tag]}</p>
          </button>
        ))}
        {!reports.length && <Empty icon="🧾" text="No reports configured." />}
      </div>

      {show("Payments") && (
        <div className="card card-pad">
          <div style={{ display: "flex", justifyContent: "space-between", gap: 10, flexWrap: "wrap", alignItems: "center", marginBottom: 14 }}>
            <h3 style={{ margin: 0 }}>💰 Payment records</h3>
            <button className="btn btn--ghost btn--sm" onClick={exportCsv} disabled={!filteredPayments.length}>
              ⬇ Export CSV
            </button>
          </div>
          {payError && <ErrorBox message={payError} />}
          {!payments && !payError && <Loading />}
          {payments && (
            <>
              <div className="form-grid" style={{ marginBottom: 14 }}>
                <div className="field">
                  <label htmlFor="pay-q">Search</label>
                  <input id="pay-q" className="input" placeholder="Player, description or ID…" value={payQuery} onChange={(e) => setPayQuery(e.target.value)} />
                </div>
                <div className="field">
                  <label htmlFor="pay-s">Status</label>
                  <select id="pay-s" className="select" value={payStatus} onChange={(e) => setPayStatus(e.target.value)}>
                    <option value="ALL">All statuses</option>
                    {payStatuses.map((s) => (
                      <option key={s} value={s}>
                        {pretty(s)}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="field">
                  <label htmlFor="pay-from">From</label>
                  <input id="pay-from" className="input" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
                </div>
                <div className="field">
                  <label htmlFor="pay-to">To</label>
                  <input id="pay-to" className="input" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
                </div>
                <button
                  className="btn btn--ghost"
                  type="button"
                  onClick={() => {
                    setPayQuery("");
                    setPayStatus("ALL");
                    setFrom("");
                    setTo("");
                  }}
                >
                  Clear filters
                </button>
              </div>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Player</th>
                      <th>Description</th>
                      <th>Date</th>
                      <th>Status</th>
                      <th style={{ textAlign: "right" }}>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredPayments.map((p) => (
                      <tr key={p.id}>
                        <td className="mono">{p.id}</td>
                        <td>{p.player}</td>
                        <td>{p.description}</td>
                        <td className="mono">{p.date}</td>
                        <td>
                          <span className={`badge ${payClass(p.status)}`}>{pretty(p.status)}</span>
                        </td>
                        <td className="mono" style={{ textAlign: "right" }}>
                          {inr(p.amount)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  {filteredPayments.length > 0 && (
                    <tfoot>
                      <tr>
                        <td colSpan={5} style={{ fontWeight: 700 }}>
                          Total ({filteredPayments.length}) · collected {inr(sum(filteredPayments, "PAID"))} · pending{" "}
                          {inr(sum(filteredPayments, "PENDING"))}
                        </td>
                        <td className="mono" style={{ textAlign: "right", fontWeight: 800 }}>
                          {inr(sum(filteredPayments))}
                        </td>
                      </tr>
                    </tfoot>
                  )}
                </table>
                {!filteredPayments.length && <Empty icon="💰" text="No payments match these filters." />}
              </div>
            </>
          )}
        </div>
      )}

      {(show("Players") || show("Tournaments")) && (
        <div className="grid-2">
          {show("Players") && (
            <div className="card card-pad">
              <h3>🏃 Registration summary</h3>
              {!players ? (
                <Loading />
              ) : !players.length ? (
                <Empty text="No players yet." />
              ) : (
                <>
                  <div className="kpi-grid" style={{ gridTemplateColumns: "repeat(auto-fit, minmax(120px, 1fr))", marginBottom: 16 }}>
                    <Mini label="Players" value={players.length} />
                    <Mini label="Registered" value={players.filter((p) => p.registered).length} />
                    <Mini label="Not registered" value={players.filter((p) => !p.registered).length} />
                  </div>
                  {SPORTS.map((s) => {
                    const n = players.filter((p) => p.preferences.includes(s)).length;
                    return (
                      <div className="bar-row" key={s}>
                        <span>{s}</span>
                        <div style={{ background: "var(--brand-soft)", borderRadius: 999 }}>
                          <div className="bar" style={{ width: `${(n / players.length) * 100}%` }} />
                        </div>
                        <span className="mono">{n}</span>
                      </div>
                    );
                  })}
                </>
              )}
            </div>
          )}

          {show("Tournaments") && (
            <div className="card card-pad">
              <div style={{ display: "flex", justifyContent: "space-between", gap: 10, flexWrap: "wrap", alignItems: "center", marginBottom: 12 }}>
                <h3 style={{ margin: 0 }}>🏆 Tournament summary</h3>
                <select className="select" style={{ maxWidth: 170 }} value={sportFilter} onChange={(e) => setSportFilter(e.target.value)} aria-label="Filter tournaments by sport">
                  <option value="ALL">All sports</option>
                  {SPORTS.map((s) => (
                    <option key={s}>{s}</option>
                  ))}
                </select>
              </div>
              {!tournaments ? (
                <Loading />
              ) : (
                <>
                  <div className="checks" style={{ marginBottom: 12 }}>
                    {TOURNAMENT_STATUSES.map((s) => (
                      <span key={s} className={`badge ${tourClass(s)}`}>
                        {pretty(s)}: {visibleTournaments.filter((t) => t.status === s).length}
                      </span>
                    ))}
                  </div>
                  <div className="table-wrap">
                    <table>
                      <thead>
                        <tr>
                          <th>Tournament</th>
                          <th>Status</th>
                          <th>Entries</th>
                          <th>Deadline</th>
                        </tr>
                      </thead>
                      <tbody>
                        {visibleTournaments.map((t) => (
                          <tr key={t.id}>
                            <td>
                              {t.name}
                              <div style={{ fontSize: 12, color: "var(--text-2)" }}>{t.sport}</div>
                            </td>
                            <td>
                              <span className={`badge ${tourClass(t.status)}`}>{pretty(t.status)}</span>
                            </td>
                            <td className="mono">
                              {t.entries}/{t.maxEntries}
                            </td>
                            <td className="mono">{t.registrationDeadline}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {!visibleTournaments.length && <Empty icon="🏆" text="No tournaments for this sport." />}
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      )}

      {(show("Facilities") || show("Equipment")) && (
        <div className="grid-2">
          {show("Facilities") && (
            <div className="card card-pad">
              <h3>📅 Facility utilisation — today</h3>
              {!schedule ? (
                <Loading />
              ) : (
                schedule.rows.map((r) => {
                  const used = r.slots.filter((s) => s.booking).length;
                  return (
                    <div className="bar-row" key={r.name} style={{ gridTemplateColumns: "160px 1fr 50px" }}>
                      <span>{r.name}</span>
                      <div style={{ background: "var(--brand-soft)", borderRadius: 999 }}>
                        <div className="bar" style={{ width: `${(used / r.slots.length) * 100}%` }} />
                      </div>
                      <span className="mono">
                        {used}/{r.slots.length}
                      </span>
                    </div>
                  );
                })
              )}
            </div>
          )}
          {show("Equipment") && (
            <div className="card card-pad">
              <h3>🎽 Equipment condition audit</h3>
              {!equipment ? (
                <Loading />
              ) : !equipment.length ? (
                <Empty text="Inventory is empty." />
              ) : (
                <div className="table-wrap">
                  <table>
                    <thead>
                      <tr>
                        <th>Item</th>
                        <th>Condition</th>
                        <th>Issued</th>
                      </tr>
                    </thead>
                    <tbody>
                      {equipment.map((e) => (
                        <tr key={e.id}>
                          <td>{e.name}</td>
                          <td>{pretty(e.condition)}</td>
                          <td className="mono">
                            {e.issuedUnits}/{e.totalUnits}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </>
  );
}

function Mini({ label, value }) {
  return (
    <div className="card kpi" style={{ boxShadow: "none", padding: "12px 14px" }}>
      <div>
        <div className="kpi-value">{value}</div>
        <div className="stat-label">{label}</div>
      </div>
    </div>
  );
}

Reports.title = "Reports";
Reports.icon = "🧾";
export default Reports;
