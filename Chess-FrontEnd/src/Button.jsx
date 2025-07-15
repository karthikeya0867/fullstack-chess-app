const Button = ({
  children,
  isButtonDark = false,
  className = " ",
  ...rest
}) => {
  const shadow = isButtonDark
    ? "0 4px 10px rgba(0, 0, 0, 0.7)"
    : "0 4px 10px rgba(255, 255, 255, 0.3)";
  return (
    <button
      className={`px-8 py-3 border-none rounded-3xl font-bold cursor-pointer 
        w-fit block mx-auto mt-3 transition duration-200 transform active:translate-y-[2px] hover:translate-y-[-2px]
         ${
           isButtonDark
             ? "bg-black text-white active:box-shadow-[0 4px 10px rgba(255, 255, 255, 0.3)]"
             : "bg-white text-black active:box-shadow-[ 0 4px 10px rgba(0, 0, 0, 0.7)]"
         } ${className}`}
      style={{
        boxShadow: shadow,
      }}
      {...rest}
    >
      {children}
    </button>
  );
};

export default Button;
