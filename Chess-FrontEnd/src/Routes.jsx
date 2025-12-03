import LandingPage from "./LandingPage";
import Auth from "./AuthForm";
import Play from "./Play";

const routes = [
  {
    path: "/login",
    element: <Auth key="login" mode="login" />,
  },
  {
    path: "/signup",
    element: <Auth key="signup" mode="signup" />,
  },
  {
    path: "/",
    element: <LandingPage />,
  },
  {
    path: "/play",
    element: <Play />,
  },
];

export default routes;
