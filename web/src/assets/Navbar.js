import { Link, useNavigate } from 'react-router-dom';
import api from './api'
import 'bootstrap/dist/css/bootstrap.min.css';

function Navbar({ isLoggedIn }) {
    const navigate = useNavigate();

    const handleLogout = () => {
        api.post("/auth/logout", {})
        .then(response => {
            alert(response.data);
            navigate('/login');
            localStorage.clear();
        }).catch(err => alert("Failed to logout", err));
    };


    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
            <div className="container-fluid">
                { isLoggedIn ?
                <>
                    <Link className="navbar-brand" to="/">Dispatcher</Link>
                    <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                        <span className="navbar-toggler-icon"></span>
                    </button>

                    <div className="collapse navbar-collapse" id="navbarNav">
                        <ul className="navbar-nav me-auto mb-2 mb-lg-0">
                            <li className="nav-item">
                                <Link className="nav-link" to="/vehicle">Vehicles</Link>
                            </li>
                            <li className="nav-item">
                                <Link className="nav-link" to="/settings">Settings</Link>
                            </li>
                        </ul>

                        <button className="btn btn-outline-light" onClick={handleLogout}>Logout</button>
                    </div>
                </>
                :
                <>
                    <Link className="navbar-brand" to="/login">Login</Link>
                    <button className="btn btn-outline-light" onClick={() => navigate('/register')}>Register</button>
                </>
                }
            </div>
        </nav>

    );
}

export default Navbar;
