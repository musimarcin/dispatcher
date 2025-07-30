import { useState } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

function Login() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        api.post("/auth/login", {
            username,
            password
        }).then(response => {
            alert(response.data);
            window.location.reload();
            window.location.href = '/';
        }).catch(err => alert(err.response?.data));
    };

    return (
        <div className="d-flex justify-content-center align-items-center mt-5">
            <div className="mw-25 p-4 border rounded shadow-lg">
                <h2 className="mb-4">Login</h2>
                <form onSubmit={handleSubmit}>
                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="p-2 mb-4 border rounded"
                        required
                    />
                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="p-2 mb-4 border rounded mx-1"
                        required
                    />
                    <button type="submit" className="btn btn-primary mb-1">
                        Login
                    </button>
                </form>
                    <button type="button" onClick={() => navigate('/register')} className="btn btn-primary">
                        Register
                    </button>
                </div>
        </div>
    );
}

export default Login;
