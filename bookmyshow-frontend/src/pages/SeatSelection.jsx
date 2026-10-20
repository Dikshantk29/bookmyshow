import { useEffect, useState } from 'react'
import { useParams, useLocation, useNavigate } from 'react-router-dom'
import api, { errMsg } from '../api'

const MAX = 6

export default function SeatSelection() {
  const { showId } = useParams()
  const { state } = useLocation()
  const navigate = useNavigate()
  const show = state?.show
  const [seats, setSeats] = useState([])
  const [selected, setSelected] = useState([])
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const load = () => api.get(`/shows/${showId}/seats`).then((r) => setSeats(r.data))
  useEffect(() => { load() }, [showId])

  const rows = seats.reduce((acc, s) => { (acc[s.seatNumber[0]] ||= []).push(s); return acc }, {})

  const toggle = (s) => {
    if (s.status !== 'AVAILABLE') return
    setError('')
    if (selected.includes(s.seatNumber)) setSelected(selected.filter((x) => x !== s.seatNumber))
    else if (selected.length >= MAX) setError(`You can select up to ${MAX} seats`)
    else setSelected([...selected, s.seatNumber])
  }

  const proceed = async () => {
    setBusy(true); setError('')
    try {
      const { data } = await api.post('/bookings/lock', { showId: Number(showId), seatNumbers: selected })
      navigate(`/checkout/${data.id}`, { state: { booking: data } })
    } catch (e) {
      setError(errMsg(e)); setSelected([]); load()
    } finally { setBusy(false) }
  }

  return (
    <>
      {show && <h2>{show.movieTitle} <span className="muted">— {show.theatreName}, {new Date(show.startTime).toLocaleString('en-IN')}</span></h2>}
      <div className="screen">SCREEN THIS WAY</div>
      <div className="seat-map">
        {Object.entries(rows).map(([row, list]) => (
          <div className="seat-row" key={row}>
            <span className="row-label">{row}</span>
            {list.map((s) => (
              <button key={s.seatNumber} onClick={() => toggle(s)} disabled={s.status !== 'AVAILABLE'}
                className={`seat ${s.status.toLowerCase()} ${selected.includes(s.seatNumber) ? 'selected' : ''}`}>
                {s.seatNumber.slice(1)}
              </button>
            ))}
          </div>
        ))}
      </div>
      <div className="legend">
        <span><i className="seat available" /> Available</span>
        <span><i className="seat selected" /> Selected</span>
        <span><i className="seat booked" /> Sold</span>
      </div>
      {error && <p className="error">{error}</p>}
      <div className="bottom-bar">
        <div>
          {selected.length > 0 ? <>Seats: <b>{selected.join(', ')}</b>{show && <> • Total: <b>₹{show.price * selected.length}</b></>}</> : 'Select your seats'}
        </div>
        <button className="btn" disabled={selected.length === 0 || busy} onClick={proceed}>
          {busy ? 'Holding seats...' : 'Proceed to pay'}
        </button>
      </div>
    </>
  )
}
