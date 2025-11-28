import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { useState, useEffect, useContext } from "react";
import { AuthContext } from "./assets/AuthContext";
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
import './App.css';
import ToastMessage from "./assets/ToastMessage";

function App() {
    const [isLoading, setIsLoading] = useState(true);
    const { user } = useContext(AuthContext);
    const [toast, setToast] = useState({ message: "", type: "info", show: false });

    const showToast = (message, type = "info") => {
        setToast({ message, type, show: true });
    };

    const hideToast = () => setToast(prev => ({ ...prev, show: false }));

    return (
        <>
            <ToastMessage
                message={toast.message}
                type={toast.type}
                show={toast.show}
                onClose={hideToast}
            />
            <Router>
                <Navbar user={user}/>
                <Routes>
                    <Route path="/" element={
                        <ProtectedRoute user={user}>
                            <Dashboard showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="/login" element={
                        <PublicRoute user={user}>
                            <Login showToast={showToast} />
                        </PublicRoute>
                    } />
                    <Route path="/register" element={
                        <PublicRoute user={user}>
                            <Register showToast={showToast} />
                        </PublicRoute>
                    } />
                    <Route path="/settings" element={
                        <ProtectedRoute user={user}>
                            <Settings showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="/vehicle" element={
                        <ProtectedRoute user={user}>
                            <Vehicle showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="/route" element={
                        <ProtectedRoute user={user}>
                            <RouteMap showToast={showToast} />
                        </ProtectedRoute>
                    } />
                    <Route path="*" element={
                        <ProtectedRoute user={user}/>
                    } />
                </Routes>
            </Router>
        </>
    );
}

export default App;