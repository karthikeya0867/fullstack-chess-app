import { Link, useNavigate } from "react-router-dom";
import VisibilityIcon from "@mui/icons-material/Visibility";
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff";
import { useState } from "react";
import Button from "./Button";
import axios from "axios"; // Import axios
import { useAuth } from "./AuthContext"; // Import useAuth

function AuthForm({ mode = "login" }) {
  const isSignup = mode === "signup";
  const [isPasswordVisible, setPasswordVisible] = useState(false);
  const [isConfirmVisible, setConfirmVisible] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const auth = useAuth(); // Use the AuthContext
  const navigate = useNavigate(); // For redirection

  const togglePasswordVisibility = () => setPasswordVisible(!isPasswordVisible);
  const toggleConfirmVisibility = () => setConfirmVisible(!isConfirmVisible);

  const API_BASE_URL = "http://localhost:8080/api/auth"; // Backend API URL

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      if (isSignup) {
        if (password !== confirmPassword) {
          setError("Passwords do not match");
          setLoading(false);
          return;
        }
        await axios.post(`${API_BASE_URL}/register`, { username, password });
        navigate("/login"); // Redirect to login after successful registration
      } else {
        const response = await axios.post(`${API_BASE_URL}/login`, { username, password });
        auth.login(response.data.token, response.data.userId); // Save token and userId
        navigate("/"); // Redirect to landing page after successful login
      }
    } catch (err) {
      console.error("Authentication error:", err);
      if (err.response && err.response.data) {
        setError(err.response.data.message || err.response.data);
      } else {
        setError("An unexpected error occurred.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <Link to="/" className="absolute top-4 left-4 z-10">
        <img
          src="./src/assets/logo.png"
          alt="Logo"
          className="h-16 sm:h-20 md:h-24 lg:h-28 xl:h-32 w-auto object-contain brightness-0 animate-fadeIn invert contrast-200"
        />
      </Link>

      <div
        className={`bg-cover bg-no-repeat bg-center h-[100vh] w-[100vw] flex justify-center items-center overflow-hidden`}
        style={{ backgroundImage: `url('./src/assets/logo1.png')` }}
      >
        <div className="animate-fadeIn  bg-black/60 p-10 rounded-2xl backdrop-blur-sm shadow-[0_0_20px_rgba(255,255,255,0.2)] w-[90%] max-w-[420px] min-h-[70%] flex;">
          <form onSubmit={handleSubmit} className="flex flex-col justify-center gap-[20px] w-full text-white items-stretch">
            <p className="font-bold text-center mb-[10px] pb-[10px] border-b-2 border-white text-[26px] text-shadow wel-text">
              {isSignup ? "Create Your Account" : "Welcome Back, Please Login."}
            </p>
            <p className="sub-text">
              {isSignup
                ? "Join us and start your journey!"
                : "Login to continue."}
            </p>
            
            <label
                htmlFor="username"
                className="flex flex-col relative cursor-pointer"
            >
                Username
                <input
                    type="text"
                    id="username"
                    className="username"
                    placeholder="Enter your username"
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    required
                />
            </label>

            <label htmlFor="password" className="cursor-pointer">
              Password
              <div className="relative flex items-center w-full">
                <input
                  type={isPasswordVisible ? "text" : "password"}
                  id="password"
                  className="pr-[2.5rem] flex-1 box-border w-full"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <span
                  className="absolute top-[50%] right-[0.75rem] translate-y-[-50%] cursor-pointer flex items-center justify-center;"
                  onClick={togglePasswordVisibility}
                >
                  {isPasswordVisible ? (
                    <VisibilityOffIcon className="cursor-pointer transform hover:scale-125" />
                  ) : (
                    <VisibilityIcon className="cursor-pointer transform hover:scale-125" />
                  )}
                </span>
              </div>
            </label>

            {isSignup && (
              <label htmlFor="confirm-password" className="cursor-pointer">
                Retype Password
                <div className="relative flex items-center w-full">
                  <input
                    type={isConfirmVisible ? "text" : "password"}
                    id="confirm-password"
                    className="pr-[2.5rem] flex-1 box-border w-full"
                    placeholder="Retype your password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                  />
                  <span
                    className="absolute top-[50%] right-[0.75rem] translate-y-[-50%] cursor-pointer flex items-center justify-center;"
                    onClick={toggleConfirmVisibility}
                  >
                    {isConfirmVisible ? (
                      <VisibilityOffIcon className="cursor-pointer transform hover:scale-125" />
                    ) : (
                      <VisibilityIcon className="cursor-pointer transform hover:scale-125" />
                    )}
                  </span>
                </div>
              </label>
            )}

            {error && <p className="text-red-500 text-center">{error}</p>}

            <div className="text-white text-[15px] text-center mt-[10px]">
              {isSignup ? (
                <>
                  Already have an account?{" "}
                  <Link to="/login" className="text-gray-400 mt-[10px]">
                    Login
                  </Link>
                </>
              ) : (
                <>
                  Don't have an account?{" "}
                  <Link to="/signup" className="mt-[10px] text-gray-400">
                    SignUp
                  </Link>
                </>
              )}
            </div>

            <Button type="submit" disabled={loading}>
              {loading ? "Loading..." : (isSignup ? "Sign Up" : "Login")}
            </Button>
          </form>
        </div>
      </div>
    </>
  );
}

export default AuthForm;
