/** @type {import('tailwindcss').Config} */
export const content = ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"];
export const theme = {
  extend: {
    keyframes: {
      fadeIn: {
        "0%": {
          opacity: 0,
          transform: "translateY(-20px)",
        },
        "100%": {
          opacity: 1,
          transform: "translateY(0)",
        },
      },
    },
    animation: {
      fadeIn: "fadeIn 0.5s ease-out",
    },
  },
};
export const plugins = [];
