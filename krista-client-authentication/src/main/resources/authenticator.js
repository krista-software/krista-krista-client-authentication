var __JSAuthFn__ = (() => {
  let baseUrl = "__BASE_URI__";
  let context = null;

  const constructUrl = (path) => {
    let url = baseUrl.replace("extension.", "");
    if (url.lastIndexOf("/") === url.length) {
      url = url + path;
    } else {
      url = url + "/" + path;
    }
    return url;
  };
  const doLogin = (clientSessionId) => {
    return new Promise((resolve, reject) => {
      fetch(constructUrl("authn/login"), {
        method: "POST",
        credentials: 'include',
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({clientSessionId}),
      })
      .then(() => {
          resolve({ status: "success", msg: "Login success"});
      })
      .catch(() => {
        reject({ status: "failed", msg: "Login failed"});
      });
    });
  };

  return {
    login: doLogin,
  };
})();

window.__JSWindowVariable__ = __JSAuthFn__;
