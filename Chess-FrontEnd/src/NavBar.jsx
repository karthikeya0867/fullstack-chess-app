import { useState, useRef, useEffect } from "react";
import { Link } from "react-router-dom";
import * as motion from "motion/react-client";

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const toggleRef = useRef(null);
  const [coords, setCoords] = useState({ x: 0, y: 0 });
  const [hasMounted, setHasMounted] = useState(false);

  useEffect(() => {
    requestAnimationFrame(() => setHasMounted(true));
  }, []);

  useEffect(() => {
    if (toggleRef.current) {
      const rect = toggleRef.current.getBoundingClientRect();
      setCoords({
        x: rect.left + rect.width / 2,
        y: rect.top + rect.height / 2,
      });
    }
  }, [isOpen]);

  useEffect(() => {
  const handleClickOutside = (e) => {
    if (isOpen && !e.target.closest(".backdrop-container") && !e.target.closest("button")) {
      setIsOpen(false);
    }
  };

  const handleScroll = () => {
    if (isOpen) setIsOpen(false);
  };

  document.addEventListener("click", handleClickOutside);
  window.addEventListener("scroll", handleScroll);

  return () => {
    document.removeEventListener("click", handleClickOutside);
    window.removeEventListener("scroll", handleScroll);
  };
}, [isOpen]);


  return (
    <header className="absolute top-0 left-0 w-full z-50 bg-transparent">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative">
        <div className="flex justify-between items-center h-16 relative">
          <Link to="/" className="flex items-center space-x-2 text-2xl font-bold text-white">
            <img
              src="./src/assets/logo.png"
              alt="Chess Logo"
              className="w-16 h-16 object-contain filter brightness-0 invert"
            />
            <span>Chess.com</span>
          </Link>

          <div className="hidden md:flex space-x-6">
            <Link to="/" className="text-white hover:text-blue-300">Home</Link>
            <Link to="/play" className="text-white hover:text-blue-300">Play</Link>
            <Link to="/login" className="text-white hover:text-blue-300">Login</Link>
            <Link to="/about" className="text-white hover:text-blue-300">About</Link>
          </div>

          <div ref={toggleRef} className="md:hidden z-50 relative">
            <MenuToggle toggle={() => setIsOpen(!isOpen)} isOpen={isOpen} />
          </div>
        </div>

        {hasMounted && (
          <motion.div
            initial="closed"
            animate={isOpen ? "open" : "closed"}
            custom={coords}
            variants={circleVariants}
            className="fixed top-0 left-0 w-full h-full z-40 pointer-events-none"
          >
            <motion.div
              initial={false}
              className="absolute inset-0 backdrop-blur-xl bg-white/10"
              variants={backgroundVariants}
              custom={coords}
              style={{ pointerEvents: isOpen ? "auto" : "none" }}
            >
              <motion.ul
                variants={menuVariants}
                initial={false}
                animate={isOpen ? "open" : "closed"}
                className="flex flex-col items-center justify-center h-full gap-6"
              >
                {menuItems.map((item, i) => (
                  <motion.li
                    key={i}
                    variants={itemVariants}
                    whileHover={{ scale: 1.1 }}
                    className="text-white text-2xl font-semibold"
                  >
                    <Link to={`/${item.text.toLowerCase()}`} onClick={() => setIsOpen(false)}>
                      {item.text}
                    </Link>
                  </motion.li>
                ))}
              </motion.ul>
            </motion.div>
          </motion.div>
        )}
      </nav>
    </header>
  );
};

const menuItems = [
  { text: "Home" },
  { text: "Play" },
  { text: "Login" },
  { text: "About" },
];

const MenuToggle = ({ toggle, isOpen }) => (
  <motion.button
    onClick={toggle}
    className="w-[50px] h-[50px] flex items-center justify-center"
    animate={isOpen ? "open" : "closed"}
    initial={false}
  >
    <svg width="23" height="23" viewBox="0 0 23 23">
      <Path
        variants={{
          closed: { d: "M 2 2.5 L 20 2.5" },
          open: { d: "M 3 16.5 L 17 2.5" },
        }}
      />
      <Path
        d="M 2 9.423 L 20 9.423"
        variants={{
          closed: { opacity: 1 },
          open: { opacity: 0 },
        }}
        transition={{ duration: 0.1 }}
      />
      <Path
        variants={{
          closed: { d: "M 2 16.346 L 20 16.346" },
          open: { d: "M 3 2.5 L 17 16.346" },
        }}
      />
    </svg>
  </motion.button>
);

const Path = (props) => (
  <motion.path
    fill="transparent"
    strokeWidth="3"
    stroke="white"
    strokeLinecap="round"
    {...props}
  />
);

const circleVariants = {
  open: () => ({ pointerEvents: "auto" }),
  closed: () => ({ pointerEvents: "none" }),
};

const backgroundVariants = {
  open: ({ x, y }) => ({
    clipPath: [
      `circle(0px at ${x}px ${y}px)`,
      `circle(400px at ${x}px ${y}px)`,
      `circle(2000px at ${x}px ${y}px)`
    ],
    transition: {
      duration: 0.8,
      ease: "easeInOut",
      times: [0, 0.4, 1],
    },
  }),
  closed: ({ x, y }) => ({
    clipPath: [
      `circle(2000px at ${x}px ${y}px)`,
      `circle(400px at ${x}px ${y}px)`,
      `circle(0px at ${x}px ${y}px)`
    ],
    transition: {
      duration: 0.6,
      ease: "easeInOut",
      times: [0, 0.4, 1],
    },
  }),
};


 const menuVariants = {
   open: {
     opacity: 1,
     transition: {
       delayChildren: 0.3,
       staggerChildren: 0.1,
     },
   },
   closed: {
     opacity: 0,
     transition: { duration: 0.2 },
   },
 };


const itemVariants = {
  open: { opacity: 1, y: 0 },
  closed: { opacity: 0, y: -10 },
};

export default Navbar;