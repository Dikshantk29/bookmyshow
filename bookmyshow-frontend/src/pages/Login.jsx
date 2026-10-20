import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import api, { errMsg } from '../api'
import { useAuth } from '../AuthContext'

export default function Login() {
  const [isRegister, setIsRegister] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const change = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { data } = await api.post(isRegister ? '/auth/register' : '/auth/login', form)
      login(data)
      navigate(location.state?.from || '/', { replace: true, state: location.state?.showState })
    } catch (err) {
      setError(errMsg(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card auth-card">
      <h2>{isRegister ? 'Create account' : 'Sign in'}</h2>
      <form onSubmit={submit}>
        {isRegister && <input name="name" placeholder="Full name" value={form.name} onChange={change} required />}
        <input name="email" type="email" placeholder="Email" value={form.email} onChange={change} required />
        <input name="password" type="password" placeholder="Password (min 6 chars)" value={form.password} onChange={change} required />
        {error && <p className="error">{error}</p>}
        <button className="btn" disabled={loading}>{loading ? 'Please wait...' : isRegister ? 'Register' : 'Login'}</button>
      </form>
      <p className="muted">
        {isRegister ? 'Already have an account?' : 'New here?'}{' '}
        <a href="#" onClick={(e) => { e.preventDefault(); setIsRegister(!isRegister); setError('') }}>
          {isRegister ? 'Sign in' : 'Create one'}
        </a>
      </p>
      {!isRegister && <p className="muted small-text">Demo: user@bms.com / user123</p>}
    </div>
  )
}
