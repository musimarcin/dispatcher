import { useState, useEffect } from "react";
import api from '../assets/api'
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';

function Settings() {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [email, setEmail] = useState("");
    const [roles, setRoles] = useState([]);
    const [selectedUserRoles, setSelectedUserRoles] = useState([]);
    const [selectedAvailableRoles, setSelectedAvailableRoles] = useState([]);
    const [userRoles, setUserRoles] = useState([]);
    const [isPopUp, setIsPopUp] = useState(false);
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
            }).catch(err => alert(err.response?.data)); //question mars to check if previous part returned null
        }

        const storedUserRoles = localStorage.getItem("userRoles");

        if (storedUserRoles) {
            const userRolesData = JSON.parse(storedUserRoles);
            setUserRoles(userRolesData);
            //filter out roles
            const allRoles = JSON.parse(storedRoles);
            const filtered = allRoles.filter(role => !userRolesData.includes(role));
            setRoles(filtered);
        } else {
            updateRoles()
                .then(() => { //filter out roles
                    const allRoles = JSON.parse(localStorage.getItem("roles") || "[]");
                    const userRolesData = JSON.parse(localStorage.getItem("userRoles") || "[]");
                    const filtered = allRoles.filter(role => !userRolesData.includes(role));
                    setRoles(filtered);
                });
        }
    }, []);

    const updateRoles = () => {
        return api.get("/auth/role/user")
            .then(response => {
                setUserRoles(response.data);
                localStorage.setItem("userRoles", JSON.stringify(response.data));
                //filter out roles
                const allRoles = JSON.parse(localStorage.getItem("roles") || "[]");
                const filtered = allRoles.filter(role => !response.data.includes(role));
                setRoles(filtered);
                setSelectedAvailableRoles([]);
            }).catch(err => alert(err.response?.data));
    };

    const handleUserRolesChange = (e) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        setSelectedUserRoles(selectedOptions);
    };

    const handleAvailableRolesChange = (e) => {
        const selectedOptions = Array.from(e.target.selectedOptions, option => option.value);
        setSelectedAvailableRoles(selectedOptions);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        api.put("/auth/change", {
            username,
            password,
            email
            }).then(navigate('/settings'))
            .catch(err => alert(err.response?.data));
    };

    const removeRoles = async (e) => {
        e.preventDefault();
        const rolesWithPrefix = selectedUserRoles.map(role => "ROLE_" + role);

        api.post("/auth/change/role/remove", {
            roles: rolesWithPrefix
        }).then(() => {
            return updateRoles();
        }).then(() => {
            setSelectedUserRoles([]); //refreshes window
        }).catch(err => alert(err.response?.data));
    };

    const addRoles = async (e) => {
        e.preventDefault();
        const rolesWithPrefix = selectedAvailableRoles.map(role => "ROLE_" + role);

        api.post("/auth/change/role/add", {
            roles: rolesWithPrefix
        }).then(() => {
            return updateRoles();
        }).then(() => {
            setSelectedAvailableRoles([]); //refreshes window
        }).catch(err => alert(err.response?.data));
    };

    const deleteUser = async (e) => {
        await api.delete('auth/delete')
        .then(response => {
            alert(response.data);
            navigate('/login');
            localStorage.clear();
        })
        .catch(err => alert(err.response?.data));
    }

    return (
        <div className="container mt-4">
            <h2 className="mb-4">Settings</h2>
            <form onSubmit={handleSubmit} className="mb-4">
                <div className="mb-3">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <input
                        type="password"
                        className="form-control"
                        placeholder="Password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    />
                </div>
                <div className="mb-3">
                    <input
                        type="email"
                        className="form-control"
                        placeholder="Email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                    />
                </div>
                <button type="submit" className="btn btn-primary">Change</button>
            </form>

            <div className="row mb-4">
                <div className="col-md-6">
                    <h4>User Roles</h4>
                    <select
                        className="form-select"
                        multiple
                        onChange={handleUserRolesChange}
                        value={selectedUserRoles}
                        size={5}
                    >
                        {userRoles.map((role) => (
                            <option key={role} value={role}>{role}</option>
                        ))}
                    </select>
                    <button className="btn btn-danger mt-2" onClick={removeRoles}>
                        Remove Selected Roles
                    </button>
                </div>

                <div className="col-md-6">
                    <h4>Available Roles</h4>
                    <select
                        className="form-select"
                        multiple
                        onChange={handleAvailableRolesChange}
                        value={selectedAvailableRoles}
                        size={5}
                    >
                        {roles.map((role) => (
                            <option key={role} value={role}>{role}</option>
                        ))}
                    </select>
                    <button className="btn btn-success mt-2" onClick={addRoles}>
                        Add Selected Roles
                    </button>
                </div>
            </div>

            <button className="btn btn-outline-danger" onClick={() => setIsPopUp(true)}>
                Delete User
            </button>

            {/* Confirmation Modal */}
            {isPopUp && (
                <div className="modal show fade d-block" tabIndex="-1">
                    <div className="modal-dialog">
                        <div className="modal-content">
                            <div className="modal-header">
                                <h5 className="modal-title">Confirm Deletion</h5>
                                <button
                                    type="button"
                                    className="btn-close"
                                    onClick={() => setIsPopUp(false)}
                                ></button>
                            </div>
                            <div className="modal-body">
                                <p>Are you sure you want to delete your account?</p>
                            </div>
                            <div className="modal-footer">
                                <button
                                    className="btn btn-danger"
                                    onClick={() => {
                                        setIsPopUp(false);
                                        deleteUser();
                                    }}
                                >
                                    Yes, delete
                                </button>
                                <button
                                    className="btn btn-secondary"
                                    onClick={() => setIsPopUp(false)}
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                    <div className="modal-backdrop fade show"></div>
                </div>
            )}
        </div>
    );
}

export default Settings;
