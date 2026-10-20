import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api'

export default function Home() {
  const [movies, setMovies] = useState([])
  const [q, setQ] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const t = setTimeout(() => {
      setLoading(true)
      api.get('/movies', { params: { q } })
        .then((r) => setMovies(r.data))
        .finally(() => setLoading(false))
    }, 300) // debounce search
    return () => clearTimeout(t)
  }, [q])

  return (
    <>
      <div className="row between">
        <h2>Now Showing</h2>
        <input className="search" placeholder="Search movies..." value={q} onChange={(e) => setQ(e.target.value)} />
      </div>
      {loading ? <p>Loading...</p> : movies.length === 0 ? <p>No movies found.</p> : (
        <div className="grid">
          {movies.map((m) => (
            <Link to={`/movie/${m.id}`} key={m.id} className="movie-card">
              <img src={m.posterUrl} alt={m.title} />
              <div className="info">
                <h4>{m.title}</h4>
                <p className="muted">{m.genre} • {m.language}</p>
                <p>⭐ {m.rating}</p>
              </div>
            </Link>
          ))}
        </div>
      )}
    </>
  )
}
