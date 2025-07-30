import React from "react";
import { Navigate } from "react-router-dom";

function ProtectedRoute({ isLoggedIn, children }) {
    if (!isLoggedIn) {
        // Not logged in, redirect to login page
        return <Navigate to="/login" replace />;
    }
  return children;
};

export default ProtectedRoute;
