const fetchData = async (url, options = {}) => {
  try {
    const result = await fetch(url, options);
    return await result.json();
  } catch (err) {
    console.error(err);
    return { error: "Error Validating Message" };
  }
};

export default fetchData;
