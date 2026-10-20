import { useEffect, useState } from 'react'
import api, { errMsg } from '../api'

export default function MyBookings() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = () => api.get('/bookings/my').then((r) => setBookings(r.data)).finally(() => setLoading(false))
  useEffect(() => { load() }, [])

  const cancel = async (id) => {
    if (!window.confirm('Cancel this booking?')) return
    try { await api.post(`/bookings/${id}/cancel`); load() } catch (e) { setError(errMsg(e)) }
  }

  if (loading) return <p>Loading...</p>
  return (
    <>
      <h2>My Bookings</h2>
      {error && <p className="error">{error}</p>}
      {bookings.length === 0 && <p className="muted">No bookings yet.</p>}
      {bookings.map((b) => (
        <div className="card row between wrap" key={b.id}>
          <div>
            <h3>{b.movieTitle}</h3>
            <p className="muted">{b.theatreName}, {b.city} • {new Date(b.startTime).toLocaleString('en-IN')}</p>
            <p>Seats: {b.seats.join(', ')} • ₹{b.totalAmount} • Ref: {b.bookingRef}</p>
          </div>
          <div style={{ textAlign: 'right' }}>
            <span className={`badge ${b.status.toLowerCase()}`}>{b.status}</span>
            {['CONFIRMED', 'PENDING'].includes(b.status) && (
              <div><button className="btn small outline" style={{ marginTop: 8 }} onClick={() => cancel(b.id)}>Cancel</button></div>
            )}
          </div>
        </div>
      ))}
    </>
  )
}
