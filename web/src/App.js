import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState, useEffect } from "react";
import api from './assets/api'
import Login from './pages/Login';
import Register from './pages/Register';
import Settings from './pages/Settings';
import Dashboard from './pages/Dashboard';
import Vehicle from './pages/Vehicle';
import Navbar from './assets/Navbar';
import ProtectedRoute from './assets/ProtectedRoute'
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        api.get("/auth/check")
        .then(res => {
            setIsLoggedIn(res.data)
        })
        .catch(() => setIsLoggedIn(false))
    });

    return (
        <>
            <Router>
                <Navbar isLoggedIn={isLoggedIn}/>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Dashboard />
                        </ProtectedRoute>
                    }/>
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
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
                    <Route path="*" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}/>
                    } />
                </Routes>
            </Router>
        </>
    );
}

export default App;