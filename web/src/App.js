import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Settings from './pages/Settings';
import Logout from './assets/Logout';

function App() {
  return (
      <>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/settings" element={<Settings />} />
          </Routes>
          <Logout />
        </Router>
      </>
  );
}

export default App;