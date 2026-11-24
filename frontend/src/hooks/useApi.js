import { useState, useEffect, useCallback } from 'react'

/** Hook that loads data with loading / error states and optional manual reload. */
export default function useApi(fn, deps = []) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(() => {
    let alive = true
    setLoading(true); setError(null)
    fn()
      .then(d => { if (alive) setData(d) })
      .catch(e => { if (alive) setError(e.message || 'Something went wrong') })
      .finally(() => { if (alive) setLoading(false) })
    return () => { alive = false }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => load(), [load])
  return { data, loading, error, reload: load, setData }
}

/** Small toast system. */
export function useToasts() {
  const [toasts, setToasts] = useState([])
  const push = useCallback((message, type = 'success') => {
    const id = Date.now() + Math.random()
    setToasts(t => [...t, { id, message, type }])
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 3500)
  }, [])
  return { toasts, push }
}
