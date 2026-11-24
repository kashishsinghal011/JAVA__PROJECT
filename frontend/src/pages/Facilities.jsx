import { useState, useEffect } from "react";
import useApi, { useToasts } from "../hooks/useApi.js";
import { api, SLOTS } from "../services/mockApi.js";
import Toasts from "../components/Toast.jsx";
import { Loading, ErrorBox } from "./Dashboard.jsx";

function Facilities() {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const { data, loading, error, reload } = useApi(
    () => api.getSchedule(date),
    [date],
  );
  const { data: bookingsData } = useApi(
    () => api.getSchedule(new Date().toISOString().slice(0, 10)),
    [],
  );
  const { toasts, push } = useToasts();
  const [form, setForm] = useState({
    facility: "",
    slot: SLOTS[0],
    bookedBy: "",
  });
  const [formError, setFormError] = useState(null);

  // keep facility default in sync once schedule loads
  useEffect(() => {
    if (data && !form.facility)
      setForm((f) => ({ ...f, facility: data.rows[0]?.name || "" }));
  }, [data]); // eslint-disable-line react-hooks/exhaustive-deps

  if (loading) return <Loading />;
  if (error) return <ErrorBox message={error} />;

  const book = async (facility, slot) => {
    try {
      if (
        form.bookedBy.trim() &&
        (form.facility !== facility || form.slot !== slot)
      ) {
        // booking from grid click: use typed name
        await api.bookSlot({ facility, date, slot, bookedBy: form.bookedBy });
      } else {
        await api.bookSlot({ facility, date, slot, bookedBy: form.bookedBy });
      }
      push("Slot booked");
      setFormError(null);
      reload();
    } catch (err) {
      setFormError(err.message);
      push(err.message, "error");
    }
  };

  const upcoming = [];
  const allBookings = [...(bookingsData?.rows || [])]
    .flatMap((r) => r.slots.map((s) => s.booking))
    .filter(Boolean)
    .filter((b) => b.status === "CONFIRMED" && b.date >= date);

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Facilities &amp; Booking</h2>
          <p>Availability grid — click a free slot to book it.</p>
        </div>
        <input
          className="input"
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          aria-label="Schedule date"
        />
      </div>

      <form
        className="card card-pad"
        onSubmit={(e) => {
          e.preventDefault();
          if (form.facility) book(form.facility, form.slot);
        }}
      >
        <h3>Book a slot</h3>
        <div className="form-grid">
          <div className="field">
            <label htmlFor="f-name">Facility</label>
            <select
              id="f-name"
              className="select"
              value={form.facility}
              onChange={(e) => setForm({ ...form, facility: e.target.value })}
            >
              {data.rows.map((r) => (
                <option key={r.name}>{r.name}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="f-slot">Time slot</label>
            <select
              id="f-slot"
              className="select"
              value={form.slot}
              onChange={(e) => setForm({ ...form, slot: e.target.value })}
            >
              {SLOTS.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="f-by">Booked by (team / player)</label>
            <input
              id="f-by"
              className="input"
              required
              value={form.bookedBy}
              onChange={(e) => setForm({ ...form, bookedBy: e.target.value })}
            />
          </div>
          <button className="btn" type="submit">
            Book slot
          </button>
        </div>
        {formError && (
          <div className="form-error" role="alert">
            {formError}
          </div>
        )}
      </form>

      {data.rows.map((row) => (
        <div className="card card-pad" key={row.name}>
          <h3>
            {row.name}{" "}
            <span className="badge badge--blue" style={{ marginLeft: 8 }}>
              {row.sport}
            </span>
          </h3>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: `repeat(${SLOTS.length}, minmax(110px, 1fr))`,
              gap: 8,
            }}
          >
            {row.slots.map((s) =>
              s.booking ? (
                <button
                  key={s.slot}
                  className="slot-btn slot-btn--busy"
                  disabled
                  title={`${s.booking.bookedBy} — click to cancel`}
                >
                  {s.slot}
                  <br />
                  {s.booking.bookedBy}
                </button>
              ) : (
                <button
                  key={s.slot}
                  className="slot-btn"
                  onClick={() => book(row.name, s.slot)}
                >
                  + {s.slot}
                </button>
              ),
            )}
          </div>
        </div>
      ))}

      <div className="legend">
        <span className="lg-free">Free — click to book</span>
        <span className="lg-busy">Booked (confirmed)</span>
      </div>

      <div className="card card-pad">
        <h3>Upcoming confirmed bookings ({allBookings.length})</h3>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Slot</th>
                <th>Facility</th>
                <th>Booked by</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {allBookings.map((b) => (
                <tr key={b.id}>
                  <td className="mono">{b.date}</td>
                  <td className="mono">{b.slot}</td>
                  <td>{b.facility}</td>
                  <td>{b.bookedBy}</td>
                  <td>
                    <button
                      className="btn btn--danger btn--sm"
                      onClick={async () => {
                        try {
                          await api.cancelBooking(b.id);
                          push("Booking cancelled");
                          reload();
                        } catch (err) {
                          push(err.message, "error");
                        }
                      }}
                    >
                      Cancel
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      <Toasts toasts={toasts} />
    </>
  );
}
Facilities.title = "Facility Availability & Booking";
Facilities.icon = "📅";
export default Facilities;
