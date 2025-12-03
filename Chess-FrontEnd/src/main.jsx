import { BrowserRouter } from 'react-router-dom'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import "./index.css"
import { AuthProvider } from './AuthContext.jsx';
import { DarkModeProvider } from './DarkModeContext.jsx'; // Import DarkModeProvider

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
        <DarkModeProvider>
            <AuthProvider>
                <App />
            </AuthProvider>
        </DarkModeProvider>
  </BrowserRouter>
  
)
