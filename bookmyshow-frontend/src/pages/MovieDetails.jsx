import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import api from '../api'

const CITIES = ['Nagpur', 'Mumbai']
const fmtDate = (d) => {
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

export default function MovieDetails() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [movie, setMovie] = useState(null)
  const [shows, setShows] = useState([])
  const [city, setCity] = useState(CITIES[0])
  const [date, setDate] = useState(fmtDate(new Date()))

  const days = [0, 1, 2].map((i) => { const d = new Date(); d.setDate(d.getDate() + i); return d })

  useEffect(() => { api.get(`/movies/${id}`).then((r) => setMovie(r.data)) }, [id])
  useEffect(() => {
    api.get(`/movies/${id}/shows`, { params: { city, date } }).then((r) => setShows(r.data))
  }, [id, city, date])

  if (!movie) return <p>Loading...</p>

  // group shows by theatre
  const byTheatre = shows.reduce((acc, s) => { (acc[s.theatreName] ||= []).push(s); return acc }, {})

  return (
    <>
      <div className="card row gap">
        <img className="poster" src={movie.posterUrl} alt={movie.title} />
        <div>
          <h1>{movie.title}</h1>
          <p className="muted">{movie.genre} • {movie.language} • {movie.durationMins} min</p>
          <p>⭐ {movie.rating} / 10</p>
          <p>{movie.description}</p>
        </div>
      </div>

      <div className="row between wrap" style={{ marginTop: 24 }}>
        <div className="row gap">
          {days.map((d) => (
            <button key={fmtDate(d)} className={`chip ${date === fmtDate(d) ? 'active' : ''}`} onClick={() => setDate(fmtDate(d))}>
              {d.toLocaleDateString('en-IN', { weekday: 'short', day: 'numeric', month: 'short' })}
            </button>
          ))}
        </div>
        <select value={city} onChange={(e) => setCity(e.target.value)}>
          {CITIES.map((c) => <option key={c}>{c}</option>)}
        </select>
      </div>

      {Object.keys(byTheatre).length === 0 ? <p className="muted">No shows available for this date/city.</p> :
        Object.entries(byTheatre).map(([name, list]) => (
          <div className="card" key={name}>
            <h3>{name}</h3>
            <div className="row gap wrap">
              {list.map((s) => (
                <button key={s.id} className="show-btn"
                  onClick={() => navigate(`/book/${s.id}`, { state: { show: s } })}>
                  {new Date(s.startTime).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
                  <small>₹{s.price} • {s.availableSeats} left</small>
                </button>
              ))}
            </div>
          </div>
        ))}
    </>
  )
}
