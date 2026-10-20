# BookMyShow Clone — Frontend (React + Vite)

React 18 · React Router 6 · Axios · Context API · plain CSS

## Run
Start the Spring Boot backend first (http://localhost:8080), then:
```bash
npm install
npm run dev      # http://localhost:5173
```
Backend URL is set in `src/api.js`.

## Features
- Register / login with JWT (stored in localStorage, attached via Axios interceptor)
- Browse & debounced search of movies
- Choose city + date, see shows grouped by theatre
- Interactive seat map (max 6 seats), sold/held seats disabled
- 5-minute seat-hold countdown, mock payment, booking confirmation
- My Bookings with cancel; protected routes with redirect-back after login

## Structure
```
src/
  api.js, AuthContext.jsx, App.jsx
  components/  Navbar, ProtectedRoute
  pages/       Home, Login, MovieDetails, SeatSelection, Checkout, MyBookings
```
