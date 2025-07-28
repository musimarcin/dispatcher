import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Settings from './pages/Settings';
import Dashboard from './pages/Dashboard';
import Vehicle from './pages/Vehicle';
import Navbar from './assets/Navbar';
import 'bootstrap/dist/css/bootstrap.min.css';

function App() {
    return (
        <>
            <Router>
                <Navbar />
                <Routes>
                    <Route path="/" element={<Dashboard />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/settings" element={<Settings />} />
                    <Route path="/vehicle" element={<Vehicle />} />
                </Routes>
            </Router>
    </>
    );
}

export default App;