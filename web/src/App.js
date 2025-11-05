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
import ToastMessage from "./assets/ToastMessage";

function App() {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    const [toast, setToast] = useState({ message: "", type: "info", show: false });

    const showToast = (message, type = "info") => {
        setToast({ message, type, show: true });
    };

    const hideToast = () => setToast(prev => ({ ...prev, show: false }));


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
            <ToastMessage
                message={toast.message}
                type={toast.type}
                show={toast.show}
                onClose={hideToast}
            />
            <Router>
                <Navbar isLoggedIn={isLoggedIn}/>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Dashboard showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="/login" element={
                        <PublicRoute isLoggedIn={isLoggedIn}>
                            <Login showToast={showToast} />
                        </PublicRoute>
                    } />
                    <Route path="/register" element={
                        <PublicRoute isLoggedIn={isLoggedIn}>
                            <Register showToast={showToast} />
                        </PublicRoute>
                    } />
                    <Route path="/settings" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Settings showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="/vehicle" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <Vehicle showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="/route" element={
                        <ProtectedRoute isLoggedIn={isLoggedIn}>
                            <RouteMap showToast={showToast} />
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