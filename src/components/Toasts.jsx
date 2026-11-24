import { useToasts } from "../hooks/useApi.js";

export default function Toasts({ toasts }) {
  if (!toasts.length) return null;
  return (
    <div className="toasts" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast--${t.type}`}>
          {t.type === "error" ? "⚠️ " : "✅ "}
          {t.message}
        </div>
      ))}
    </div>
  );
}

export { useToasts };
