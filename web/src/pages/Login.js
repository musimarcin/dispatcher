import { useState } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';

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
            navigate('/settings');
        }).catch(err => alert(err.response?.data));
    };

    const goRegister = () => {
        navigate('/register')
    }

    return (
        <div className="max-w-md mx-auto mt-20 p-4 border rounded-lg shadow-lg">
            <h2 className="text-2xl mb-4">Login</h2>
            <form onSubmit={handleSubmit}>
                <input
                    type="text"
                    placeholder="Username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="w-full p-2 mb-4 border rounded"
                    required
                />
                <input
                    type="password"
                    placeholder="Password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full p-2 mb-4 border rounded"
                    required
                />
                <button type="submit" className="w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                    Login
                </button>
            </form>
                <button type="button" onClick={goRegister} className="w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                    Register
                </button>
        </div>
    );
}

export default Login;
