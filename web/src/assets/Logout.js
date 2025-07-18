import api from './api'
import { useNavigate } from 'react-router-dom';


function Logout() {

    const navigate = useNavigate();

    const handleLogout = async (e) => {
            e.preventDefault();

            api.post("/auth/logout", {})
            .catch(err => alert("Failed to logout", err));
            alert("Log out successfully")
            navigate('/login');
    };

    return (
        <button type="button" onClick={handleLogout} className="w-full p-2 bg-blue-500 text-white rounded hover:bg-blue-600">
            Logout
        </button>
    );


}
export default Logout;