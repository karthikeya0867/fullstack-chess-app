import { Link } from 'react-router-dom';
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import { useState } from 'react';
import Button from './Button.jsx';

function AuthForm({ mode = "login" }) {
    const isSignup = mode === "signup";
    const [isPasswordVisible, setPasswordVisible] = useState(false);
    const [isConfirmVisible, setConfirmVisible] = useState(false);

    const togglePasswordVisibility = () => setPasswordVisible(!isPasswordVisible);
    const toggleConfirmVisibility = () => setConfirmVisible(!isConfirmVisible);

    return (
        <>
            <Link to="/" className="absolute top-4 left-4 z-10">
                <img
                    src="./src/assets/logo.png"
                    alt="Logo"
                    className="h-16 sm:h-20 md:h-24 lg:h-28 xl:h-32 w-auto object-contain brightness-0 animate-fadeIn invert contrast-200"
                />
            </Link>

            <div className={`bg-cover bg-no-repeat bg-center h-[100vh] w-[100vw] flex justify-center items-center overflow-hidden`} style={{ backgroundImage: `url('./src/assets/logo1.png')` }}>
                <div className="animate-fadeIn  bg-black/60 p-10 rounded-2xl backdrop-blur-sm shadow-[0_0_20px_rgba(255,255,255,0.2)] w-[90%] max-w-[420px] min-h-[70%] flex;">
                    <div className="flex flex-col justify-center gap-[20px] w-full text-white items-stretch">
                        <p className="font-bold text-center mb-[10px] pb-[10px] border-b-2 border-white text-[26px] text-shadow wel-text">{isSignup ? "Create Your Account" : "Welcome Back, Please Login."}</p>
                        <p className="sub-text">{isSignup ? "Join us and start your journey!" : "Login to continue."}</p>
                        {isSignup ? (
                            <>
                                <label htmlFor="email" className='flex flex-col relative cursor-pointer'>
                                    Email
                                    <input type="email" id="email" className="username" placeholder="Enter your email" />
                                </label>

                                <label htmlFor="username" className='flex flex-col relative cursor-pointer'>
                                    Username
                                    <input type="text" id="username" className="username" placeholder="Enter your username" />
                                </label>
                            </>
                        ) : (
                            <label htmlFor="identifier" className='flex flex-col relative cursor-pointer'>
                                Email or Username
                                <input type="text" id="identifier" className="username" placeholder="Enter email or username" />
                            </label>
                        )}


                        <label htmlFor="password" className='cursor-pointer'>
                            Password
                            <div className="relative flex items-center w-full">
                                <input
                                    type={isPasswordVisible ? 'text' : 'password'}
                                    id="password"
                                    className="pr-[2.5rem] flex-1 box-border w-full"
                                    placeholder="Enter your password"
                                />
                                <span className="absolute top-[50%] right-[0.75rem] translate-y-[-50%] cursor-pointer flex items-center justify-center;" onClick={togglePasswordVisibility}>
                                    {isPasswordVisible ? <VisibilityOffIcon className="cursor-pointer transform hover:scale-125" /> :
                                        <VisibilityIcon className="cursor-pointer transform hover:scale-125" />}
                                </span>
                            </div>
                        </label>

                        {isSignup && (
                            <label htmlFor="confirm-password" className='cursor-pointer'>
                                Retype Password
                                <div className="relative flex items-center w-full">
                                    <input
                                        type={isConfirmVisible ? 'text' : 'password'}
                                        id="confirm-password"
                                        className="pr-[2.5rem] flex-1 box-border w-full"
                                        placeholder="Retype your password"
                                    />
                                    <span className="absolute top-[50%] right-[0.75rem] translate-y-[-50%] cursor-pointer flex items-center justify-center;" onClick={toggleConfirmVisibility}>
                                        {isConfirmVisible ? <VisibilityOffIcon className="cursor-pointer transform hover:scale-125" /> :
                                            <VisibilityIcon className="cursor-pointer transform hover:scale-125" />}
                                    </span>
                                </div>
                            </label>
                        )}

                        <div className="text-white text-[15px] text-center mt-[10px]">
                            {isSignup ? (
                                <>Already have an account? <Link to="/login" className="text-gray-400 mt-[10px]">Login</Link></>
                            ) : (
                                <>Don't have an account? <Link to="/signup" className="mt-[10px] text-gray-400">SignUp</Link></>
                            )}
                        </div>

                        <Button>
                            {isSignup ? "Sign Up" : "Login"}
                        </Button>
                    </div>
                </div>
            </div>
        </>
    );
}

export default AuthForm;