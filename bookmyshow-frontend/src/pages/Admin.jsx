import { useEffect, useState } from 'react'
import api, { errMsg } from '../api'

const emptyMovie = { title: '', description: '', genre: '', language: '', durationMins: '', rating: '', posterUrl: '', releaseDate: '' }
const emptyTheatre = { name: '', city: '', address: '' }
const emptyShow = { movieId: '', theatreId: '', screenName: '', startTime: '', price: '' }

function Field({ label, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
    </label>
  )
}

export default function Admin() {
  const [tab, setTab] = useState('movie')
  const [movies, setMovies] = useState([])
  const [theatres, setTheatres] = useState([])
  const [movie, setMovie] = useState(emptyMovie)
  const [theatre, setTheatre] = useState(emptyTheatre)
  const [show, setShow] = useState(emptyShow)
  const [msg, setMsg] = useState({ type: '', text: '' })
  const [busy, setBusy] = useState(false)

  const loadLists = () => {
    api.get('/movies').then((r) => setMovies(r.data))
    api.get('/theatres').then((r) => setTheatres(r.data))
  }
  useEffect(loadLists, [])

  const switchTab = (t) => { setTab(t); setMsg({ type: '', text: '' }) }
  const bind = (state, setState) => (e) => setState({ ...state, [e.target.name]: e.target.value })

  const submit = async (e, url, payload, reset, successText) => {
    e.preventDefault()
    setBusy(true)
    setMsg({ type: '', text: '' })
    try {
      await api.post(url, payload)
      setMsg({ type: 'ok', text: successText })
      reset()
      loadLists()
    } catch (err) {
      setMsg({ type: 'err', text: errMsg(err) })
    } finally {
      setBusy(false)
    }
  }

  const addMovie = (e) => submit(e, '/admin/movies', {
    ...movie,
    durationMins: Number(movie.durationMins),
    rating: movie.rating === '' ? null : Number(movie.rating),
    releaseDate: movie.releaseDate || null,
  }, () => setMovie(emptyMovie), `Movie "${movie.title}" added`)

  const addTheatre = (e) => submit(e, '/admin/theatres', theatre,
    () => setTheatre(emptyTheatre), `Theatre "${theatre.name}" added`)

  const addShow = (e) => submit(e, '/admin/shows', {
    movieId: Number(show.movieId),
    theatreId: Number(show.theatreId),
    screenName: show.screenName,
    startTime: show.startTime,
    price: Number(show.price),
  }, () => setShow(emptyShow), 'Show added (80 seats created)')

  return (
    <>
      <h2>Admin Dashboard</h2>
      <div className="row gap">
        {[['movie', 'Add Movie'], ['theatre', 'Add Theatre'], ['show', 'Add Show']].map(([key, label]) => (
          <button key={key} className={`chip ${tab === key ? 'active' : ''}`} onClick={() => switchTab(key)}>{label}</button>
        ))}
      </div>

      {msg.text && <p className={msg.type === 'ok' ? 'success' : 'error'}>{msg.text}</p>}

      {tab === 'movie' && (
        <form className="card admin-form" onSubmit={addMovie}>
          <Field label="Title *"><input name="title" value={movie.title} onChange={bind(movie, setMovie)} required /></Field>
          <Field label="Description"><textarea name="description" rows="3" value={movie.description} onChange={bind(movie, setMovie)} /></Field>
          <div className="two-col">
            <Field label="Genre"><input name="genre" value={movie.genre} onChange={bind(movie, setMovie)} /></Field>
            <Field label="Language"><input name="language" value={movie.language} onChange={bind(movie, setMovie)} /></Field>
            <Field label="Duration (minutes) *"><input name="durationMins" type="number" min="1" value={movie.durationMins} onChange={bind(movie, setMovie)} required /></Field>
            <Field label="Rating (0-10)"><input name="rating" type="number" step="0.1" min="0" max="10" value={movie.rating} onChange={bind(movie, setMovie)} /></Field>
            <Field label="Release date"><input name="releaseDate" type="date" value={movie.releaseDate} onChange={bind(movie, setMovie)} /></Field>
            <Field label="Poster URL"><input name="posterUrl" placeholder="https://placehold.co/300x450?text=Title" value={movie.posterUrl} onChange={bind(movie, setMovie)} /></Field>
          </div>
          <button className="btn" disabled={busy}>{busy ? 'Saving...' : 'Add movie'}</button>
        </form>
      )}

      {tab === 'theatre' && (
        <form className="card admin-form" onSubmit={addTheatre}>
          <Field label="Theatre name *"><input name="name" value={theatre.name} onChange={bind(theatre, setTheatre)} required /></Field>
          <Field label="City *"><input name="city" value={theatre.city} onChange={bind(theatre, setTheatre)} required /></Field>
          <Field label="Address"><input name="address" value={theatre.address} onChange={bind(theatre, setTheatre)} /></Field>
          <button className="btn" disabled={busy}>{busy ? 'Saving...' : 'Add theatre'}</button>
        </form>
      )}

      {tab === 'show' && (
        <form className="card admin-form" onSubmit={addShow}>
          <div className="two-col">
            <Field label="Movie *">
              <select name="movieId" value={show.movieId} onChange={bind(show, setShow)} required>
                <option value="">Select movie</option>
                {movies.map((m) => <option key={m.id} value={m.id}>{m.title}</option>)}
              </select>
            </Field>
            <Field label="Theatre *">
              <select name="theatreId" value={show.theatreId} onChange={bind(show, setShow)} required>
                <option value="">Select theatre</option>
                {theatres.map((t) => <option key={t.id} value={t.id}>{t.name} ({t.city})</option>)}
              </select>
            </Field>
            <Field label="Screen name *"><input name="screenName" placeholder="Audi 1" value={show.screenName} onChange={bind(show, setShow)} required /></Field>
            <Field label="Ticket price (₹) *"><input name="price" type="number" min="1" value={show.price} onChange={bind(show, setShow)} required /></Field>
            <Field label="Start date & time *"><input name="startTime" type="datetime-local" value={show.startTime} onChange={bind(show, setShow)} required /></Field>
          </div>
          <button className="btn" disabled={busy}>{busy ? 'Saving...' : 'Add show'}</button>
        </form>
      )}

      <div className="row gap wrap" style={{ alignItems: 'flex-start', marginTop: 24 }}>
        <div className="card" style={{ flex: 1, minWidth: 260 }}>
          <h3>Movies ({movies.length})</h3>
          {movies.map((m) => <p key={m.id} className="muted">{m.title} — {m.language}</p>)}
        </div>
        <div className="card" style={{ flex: 1, minWidth: 260 }}>
          <h3>Theatres ({theatres.length})</h3>
          {theatres.map((t) => <p key={t.id} className="muted">{t.name} — {t.city}</p>)}
        </div>
      </div>
    </>
  )
}