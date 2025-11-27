import { useState, useEffect } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import Dropdown from 'react-bootstrap/Dropdown';
import 'bootstrap/dist/css/bootstrap.min.css';

function Register({showToast}) {
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
            api.get("/user/roles")
            .then(response => {
                if (response.data.body == null) {
                    showToast(response.data.message, "error")
                    return;
                }
                setRoles(response.data.body);
                localStorage.setItem("roles", JSON.stringify(response.data.body));
            }).catch((err) => console.log(err));
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
            showToast(response.data.message, "success");
            setTimeout(navigate('/login'), 8000);
        }).catch(err => showToast(err.response?.data.message, "error"));
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
