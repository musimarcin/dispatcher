import { useState, useEffect } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import Dropdown from 'react-bootstrap/Dropdown';
import 'bootstrap/dist/css/bootstrap.min.css';

function Register() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [showSelected, setShowSelected] = useState("");
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
            }).catch(err => alert(err.response?.data));
        }
    }, []);

    const handleRolesChange = (role) => {
        setShowSelected(role);
        setSelectedRole("ROLE_" + role);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        api.post("/auth/register", {
            username,
            password,
            email,
            roles: [selectedRole]
        }).then(response => {
            alert(response.data);
            navigate('/login');
        }).catch(err => alert(err.response?.data));
    };

    return (
        <div className="d-flex justify-content-center align-items-center mt-5">
            <div className="p-4 border rounded-lg shadow-lg">
                <h2 className="mb-4">Register</h2>
                <form onSubmit={handleSubmit}>

                    <input
                        type="text"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        className="p-2 border rounded"
                        required
                    />

                    <input
                        type="password"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="p-2 border rounded mx-1"
                        required
                    />

                    <input
                        type="email"
                        placeholder="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="p-2 border rounded"
                        required
                    />

                    <Dropdown onSelect={handleRolesChange} className="mt-2">
                        <Dropdown.Toggle variant="primary" id="dropdown-basic">
                            {showSelected || "Select Role"}
                        </Dropdown.Toggle>
                        <Dropdown.Menu>
                            {roles.map(role => (
                                <Dropdown.Item key={role} eventKey={role}>
                                    {role}
                                </Dropdown.Item>
                            ))}
                        </Dropdown.Menu>
                    </Dropdown>
                    <button type="submit" className="btn btn-primary mt-2">
                        Register
                    </button>
                </form>
            </div>
        </div>
    );
}

export default Register;
