import { useState } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SPORTS } from "../services/mockApi.js";
import Toasts from "../components/Toast.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

function Coaches() {
  const {
    data: coaches,
    loading,
    error,
    reload,
  } = useApi(() => api.getCoaches(), []);
  const { toasts, push } = useToasts();
  const [form, setForm] = useState({
    name: "",
    specialty: SPORTS[0],
    yearsExperience: "",
  });
  const [formError, setFormError] = useState(null);
  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const submit = async (e) => {
    e.preventDefault();
    try {
      await api.addCoach({
        ...form,
        yearsExperience: Number(form.yearsExperience),
      });
      push("Coach added");
      setForm({ name: "", specialty: SPORTS[0], yearsExperience: "" });
      setFormError(null);
      reload();
    } catch (err) {
      setFormError(err.message);
    }
  };

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Coaches</h2>
          <p>Coaching staff assigned to each sport discipline.</p>
        </div>
      </div>
      <form className="card card-pad" onSubmit={submit}>
        <h3>Add coach</h3>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="c-name">Full name</label>
            <input
              id="c-name"
              className="input"
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </div>
          <div className="field">
            <label htmlFor="c-sport">Specialty</label>
            <select
              id="c-sport"
              className="select"
              value={form.specialty}
              onChange={(e) => setForm({ ...form, specialty: e.target.value })}
            >
              {SPORTS.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="c-exp">Years of experience</label>
            <input
              id="c-exp"
              className="input"
              type="number"
              min="0"
              required
              value={form.yearsExperience}
              onChange={(e) =>
                setForm({ ...form, yearsExperience: e.target.value })
              }
            />
          </div>
          <button className="btn" type="submit">
            + Add coach
          </button>
        </div>
        {formError && (
          <div className="form-error" role="alert">
            {formError}
          </div>
        )}
      </form>
      <div className="grid-cards">
        {coaches.map((c) => (
          <div className="card tile" key={c.id}>
            <div className="tile-icon">🎓</div>
            <h4>{c.name}</h4>
            <p>
              {c.specialty} coach · {c.yearsExperience} yrs experience
            </p>
            <p className="mono" style={{ marginTop: 6 }}>
              {c.id} · {c.contact}
            </p>
          </div>
        ))}
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}
Coaches.title = "Coach Management";
Coaches.icon = "🎓";
export default Coaches;
