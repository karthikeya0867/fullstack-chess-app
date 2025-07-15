import LandingPage from './LandingPage.jsx';
import Auth from './AuthForm.jsx';
import Play from './Play.jsx';

const routes = [
  {
    path: '/login',
    element: <Auth key="login" mode="login"/>,
  },
  {
    path: '/signup',
    element: <Auth key="signup" mode="signup" />,
  },
  {
    path: '/',
    element: <LandingPage />,
  },
  {
    path: '/play',
    element: <Play />,
  }
];

export default routes;
