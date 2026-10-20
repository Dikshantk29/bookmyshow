import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../AuthContext'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  return (
    <header className="navbar">
      <Link to="/" className="logo">book<span>my</span>show</Link>
      <nav>
        {user ? (
          <>
            {user.role === 'ADMIN' && <Link to="/admin">Admin</Link>}
            <Link to="/my-bookings">My Bookings</Link>
            <span className="hello">Hi, {user.name.split(' ')[0]}</span>
            <button className="btn small" onClick={() => { logout(); navigate('/') }}>Logout</button>
          </>
        ) : (
          <Link to="/login" className="btn small">Sign in</Link>
        )}
      </nav>
    </header>
  )
}