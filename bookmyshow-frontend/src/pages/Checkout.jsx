import { useEffect, useState } from 'react'
import { useParams, useLocation, Link, useNavigate } from 'react-router-dom'
import api, { errMsg } from '../api'

export default function Checkout() {
  const { id } = useParams()
  const { state } = useLocation()
  const navigate = useNavigate()
  const [booking, setBooking] = useState(state?.booking || null)
  const [secondsLeft, setSecondsLeft] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => { if (!booking) api.get(`/bookings/${id}`).then((r) => setBooking(r.data)).catch((e) => setError(errMsg(e))) }, [id])

  useEffect(() => {
    if (booking?.status !== 'PENDING' || !booking.expiresAt) return
    const tick = () => setSecondsLeft(Math.max(0, Math.floor((new Date(booking.expiresAt) - new Date()) / 1000)))
    tick()
    const t = setInterval(tick, 1000)
    return () => clearInterval(t)
  }, [booking])

  const pay = async () => {
    setBusy(true); setError('')
    try {
      const { data } = await api.post(`/bookings/${id}/pay`, { paymentMethod: 'MOCK_UPI' })
      setBooking(data)
    } catch (e) { setError(errMsg(e)) } finally { setBusy(false) }
  }

  const cancel = async () => {
    try { await api.post(`/bookings/${id}/cancel`); navigate('/') } catch (e) { setError(errMsg(e)) }
  }

  if (!booking) return error ? <p className="error">{error}</p> : <p>Loading...</p>
  const mm = String(Math.floor((secondsLeft ?? 0) / 60)).padStart(2, '0')
  const ss = String((secondsLeft ?? 0) % 60).padStart(2, '0')

  return (
    <div className="card ticket">
      <h2>{booking.status === 'CONFIRMED' ? '🎉 Booking confirmed!' : 'Review & pay'}</h2>
      <p><b>{booking.movieTitle}</b></p>
      <p>{booking.theatreName}, {booking.city} • {booking.screenName}</p>
      <p>{new Date(booking.startTime).toLocaleString('en-IN')}</p>
      <p>Seats: <b>{booking.seats.join(', ')}</b></p>
      <p>Booking ID: <b>{booking.bookingRef}</b></p>
      <h3>Total: ₹{booking.totalAmount}</h3>
      {error && <p className="error">{error}</p>}

      {booking.status === 'PENDING' && (
        secondsLeft === 0 ? (
          <><p className="error">Seat hold expired.</p><Link className="btn" to="/">Start again</Link></>
        ) : (
          <>
            <p className="timer">Complete payment in {mm}:{ss}</p>
            <div className="row gap">
              <button className="btn" onClick={pay} disabled={busy}>{busy ? 'Processing...' : `Pay ₹${booking.totalAmount} (demo)`}</button>
              <button className="btn outline" onClick={cancel}>Cancel</button>
            </div>
          </>
        )
      )}
      {booking.status === 'CONFIRMED' && <Link className="btn" to="/my-bookings">View my bookings</Link>}
      {['CANCELLED', 'EXPIRED'].includes(booking.status) && <p className="error">This booking is {booking.status.toLowerCase()}.</p>}
    </div>
  )
}
