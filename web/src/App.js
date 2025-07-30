import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState, useEffect } from "react";
import api from './assets/api'
import Login from './pages/Login';
import Register from './pages/Register';
import Settings from './pages/Settings';
import Dashboard from './pages/Dashboard';
import Vehicle from './pages/Vehicle';
import RouteMap from './pages/Route';
import Navbar from './assets/Navbar';
import ProtectedRoute from './assets/ProtectedRoute'
import PublicRoute from './assets/PublicRoute'
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        api.get("/auth/check")
        .then(res => {
            setIsLoggedIn(res.data)
        })
        .catch(() => setIsLoggedIn(false))
        .finally(() => setIsLoading(false))
    });

    if (isLoading) {
        return (
            <div className="d-flex justify-content-center align-items-center vh-100">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    return (
        <>
            <Router>
                <Navbar isLoggedIn={isLoggedIn}/>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Dashboard />
                        </ProtectedRoute>
                    } />
                    <Route path="/login" element={
                        <PublicRoute isLoggedIn={isLoggedIn}>
                            <Login />
                        </PublicRoute>
                    } />
                    <Route path="/register" element={
                        <PublicRoute isLoggedIn={isLoggedIn}>
                            <Register />
                        </PublicRoute>
                    } />
                    <Route path="/settings" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Settings />
                        </ProtectedRoute>
                    } />
                    <Route path="/vehicle" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Vehicle />
                        </ProtectedRoute>
                    } />
                    <Route path="/route" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <RouteMap />
                        </ProtectedRoute>
                    } />
                    <Route path="*" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}/>
                    } />
                </Routes>
            </Router>
        </>
    );
}

export default App;