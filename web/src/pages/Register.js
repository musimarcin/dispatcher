import { useState, useEffect } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import Dropdown from 'react-bootstrap/Dropdown';

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [roles, setRoles] = useState([]);
    const [selectedRole, setSelectedRole] = useState([]);
    const navigate = useNavigate();


    useEffect(() => {
        const storedRoles = localStorage.getItem("roles");
        if (storedRoles) {
            setRoles(JSON.parse(storedRoles))
        } else {
            api.get("/auth/roles")
            .then(response => {
                setRoles(response.data);
                localStorage.setItem("roles", JSON.stringify(response.data));
            }).catch(err => alert(err.response?.data?.message));
        }
    }, []);

    const handleRolesChange = (role) => {
        setSelectedRole("ROLE_" + role);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        api.post("/auth/register", {
            username,
            password,
            email,
            roles: [selectedRole]
        }).catch(err => console.error("Register failed", err));
        alert("Successfully registered")
        navigate('/login');
    };

    return (
        <div className="max-w-md mx-auto mt-20 p-4 border rounded-lg shadow-lg">
            <h2 className="text-2xl mb-4">Register</h2>
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

                <input
                    type="email"
                    placeholder="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="w-full p-2 mb-4 border rounded"
                    required
                />

                <Dropdown onSelect={handleRolesChange}>
                    <Dropdown.Toggle variant="primary" id="dropdown-basic">
                        {"Select Role"}
                    </Dropdown.Toggle>
                    <Dropdown.Menu>
                        {roles.map(role => (
                            <Dropdown.Item key={role} eventKey={role}>
                                {role}
                            </Dropdown.Item>
                        ))}
                    </Dropdown.Menu>
                </Dropdown>
                <button type="submit" className="p-20 w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                    Register
                </button>
            </form>
        </div>
    );
}

export default Register;
