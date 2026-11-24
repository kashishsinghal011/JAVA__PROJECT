import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SPORTS } from "../services/mockApi.js";
import Toasts from "../components/Toast.jsx";
import Empty from "../components/Empty.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

function Players() {
  const {
    data: players,
    loading,
    error,
    reload,
  } = useApi(() => api.getPlayers(), []);
  const { toasts, push } = useToasts();
  const [query, setQuery] = useState("");
  const [showForm, setShowForm] = useState(false);
  const [editing, setEditing] = useState(null);
  const [formError, setFormError] = useState(null);

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const filtered = players.filter((p) =>
    [p.name, p.rollNo, p.id]
      .join(" ")
      .toLowerCase()
      .includes(query.toLowerCase()),
  );

  const handleSubmit = async (payload) => {
    try {
      if (editing) {
        await api.updatePlayer(editing.id, payload);
        push("Player updated");
      } else {
        await api.addPlayer(payload);
        push("Player added");
      }
      setFormError(null);
      setShowForm(false);
      setEditing(null);
      reload();
    } catch (e) {
      setFormError(e.message);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Players</h2>
          <p>Student athletes, preferences and sports-office registration.</p>
        </div>
        <button
          className="btn"
          onClick={() => {
            setEditing(null);
            setFormError(null);
            setShowForm(true);
          }}
        >
          + Add player
        </button>
      </div>
      <input
        className="input search"
        placeholder="Search by name, roll no or id…"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
        aria-label="Search players"
      />
      {showForm || editing ? (
        <PlayerForm
          initial={editing}
          error={formError}
          onCancel={() => {
            setShowForm(false);
            setEditing(null);
            setFormError(null);
          }}
          onSubmit={handleSubmit}
        />
      ) : null}
      <div className="card table-card table-wrap">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Roll No</th>
              <th>Age</th>
              <th>Preferences</th>
              <th>Registered</th>
              <th>W/L/D</th>
              <th>Points</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((p) => (
              <tr key={p.id}>
                <td className="mono">{p.id}</td>
                <td>{p.name}</td>
                <td className="mono">{p.rollNo}</td>
                <td>{p.age}</td>
                <td>{p.preferences.join(", ") || "—"}</td>
                <td>
                  <span
                    className={`badge ${p.registered ? "badge--green" : "badge--gray"}`}
                  >
                    {p.registered ? "Registered" : "Not registered"}
                  </span>
                </td>
                <td className="mono">
                  {p.matchesWon}/{p.matchesLost}/{p.matchesDrawn}
                </td>
                <td className="mono">{p.pointsScored}</td>
                <td>
                  <div className="actions">
                    <button
                      className="btn btn--ghost btn--sm"
                      onClick={() => {
                        setFormError(null);
                        setEditing(p);
                        setShowForm(false);
                      }}
                    >
                      Edit
                    </button>
                    <button
                      className="btn btn--ghost btn--sm"
                      onClick={async () => {
                        await api.toggleRegistration(p.id);
                        push(
                          p.registered
                            ? "Registration revoked"
                            : "Player registered",
                        );
                        reload();
                      }}
                    >
                      {p.registered ? "Deregister" : "Register"}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {!filtered.length && <Empty text="No players match your search." />}
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}

function PlayerForm({ initial, error, onSubmit, onCancel }) {
  const [form, setForm] = useState(
    initial
      ? {
          name: initial.name,
          age: initial.age,
          rollNo: initial.rollNo,
          preferences: initial.preferences,
        }
      : { name: "", age: "", rollNo: "", preferences: [] },
  );
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const togglePref = (s) =>
    setForm((f) => ({
      ...f,
      preferences: f.preferences.includes(s)
        ? f.preferences.filter((x) => x !== s)
        : [...f.preferences, s],
    }));
  return (
    <form
      className="card card-pad"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit({
          name: form.name,
          age: Number(form.age),
          rollNo: form.rollNo,
          preferences: form.preferences,
        });
      }}
    >
      <h3>{initial ? `Edit ${initial.name}` : "New player"}</h3>
      <div className="form-grid">
        <div className="field">
          <label htmlFor="p-name">Full name</label>
          <input
            id="p-name"
            className="input"
            required
            value={form.name}
            onChange={(e) => set("name", e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="p-age">Age (16–40)</label>
          <input
            id="p-age"
            className="input"
            type="number"
            min="16"
            max="40"
            required
            value={form.age}
            onChange={(e) => set("age", e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="p-roll">Roll number</label>
          <input
            id="p-roll"
            className="input"
            required
            value={form.rollNo}
            onChange={(e) => set("rollNo", e.target.value)}
          />
        </div>
      </div>
      <div className="field" style={{ marginTop: 14 }}>
        <label>Sport preferences</label>
        <div className="checks">
          {SPORTS.map((s) => (
            <label key={s}>
              <input
                type="checkbox"
                checked={form.preferences.includes(s)}
                onChange={() => togglePref(s)}
              />{" "}
              {s}
            </label>
          ))}
        </div>
      </div>
      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}
      <div className="form-actions">
        <button className="btn" type="submit">
          {initial ? "Save changes" : "Add player"}
        </button>
        <button className="btn btn--ghost" type="button" onClick={onCancel}>
          Cancel
        </button>
      </div>
    </form>
  );
}

Players.title = "Player Management";
Players.icon = "🏃";
export default Players;
