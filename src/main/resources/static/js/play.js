  // DOM 元素
    const playButton = document.getElementById("playButton");
    const audio = document.getElementById("audioPlayer");
    const playAtInput = document.getElementById("playAtInput");
    const adjustPlus = document.getElementById("adjustPlus");
    const adjustMinus = document.getElementById("adjustMinus");
    const timeDisplay = document.getElementById("timeDisplay");
    const statusDisplay = document.getElementById("statusDisplay");

    // 状态变量
    let playAt = null;           // 播放启动时间
    let localOffset = 0;         // 本地时间偏移（用于校准）
    let isPlaying = false;       // 当前是否正在播放
    const OFFSET_STEP = 100;     // 微调步长：100ms

    // 辅助函数：时间补零
    function pad(n, w) {
      return String(n).padStart(w, "0");
    }

    // 格式化时间戳为 HH:MM:SS:ms
    function formatTime(ms) {
      const d = new Date(ms);
      return `${pad(d.getHours(), 2)}:${pad(d.getMinutes(), 2)}:${pad(d.getSeconds(), 2)}:${pad(d.getMilliseconds(), 3)}`;
    }

    // 默认设定播放时间为当前时间 +3 秒
    function setDefaultPlayAt() {
      const now = new Date();
      now.setSeconds(now.getSeconds() + 3);
      now.setMilliseconds(0);
      playAt = now;
      updatePlayAtInput();
      isPlaying = false;
    }

    // 更新输入框显示设定时间
    function updatePlayAtInput() {
      if (playAt) {
        playAtInput.value = formatTime(playAt.getTime());
      }
    }

    // 解析用户输入为 Date 对象
    function parsePlayAtInput() {
      const now = new Date();
      const parts = playAtInput.value.split(":").map(p => parseInt(p));
      if (parts.length !== 4 || parts.some(isNaN)) return null;
      return new Date(now.getFullYear(), now.getMonth(), now.getDate(), parts[0], parts[1], parts[2], parts[3]);
    }

    // 启动播放（跳转到指定时间）
    function startPlayback(fromMs = 0) {
      audio.currentTime = fromMs / 1000;
      audio.play().then(() => {
        playButton.textContent = "暂停";
        isPlaying = true;
      }).catch(err => {
        statusDisplay.textContent = `⚠️ 播放失败：${err.message}`;
      });
    }

    // 检查是否到达播放时间，并自动开始播放
    function checkAndStartPlayback() {
      if (!playAt || isNaN(audio.duration)) return;

      const now = Date.now();
      const adjustedPlayAt = playAt.getTime() + localOffset;
      const diff = now - adjustedPlayAt;
      const audioTotal = audio.duration * 1000;

      if (!isPlaying) {
        if (diff >= 0 && diff <= audioTotal) {
          startPlayback(diff);
        } else if (diff > audioTotal) {
          statusDisplay.textContent = "⏰ 错过播放时间，已超过音频时长";
          isPlaying = true;
        }
      }
    }

    // 更新状态栏显示
    function updateStatus() {
      const now = Date.now();
      const offsetStr = (localOffset >= 0 ? "+" : "") + localOffset + "ms";
      timeDisplay.innerHTML = `🕒 当前时间：${formatTime(now)} ｜ 本地偏移：${offsetStr}`;

      const audioTime = audio.currentTime.toFixed(3);
      const totalTime = isNaN(audio.duration) ? "加载中..." : audio.duration.toFixed(3) + "s";
      statusDisplay.innerHTML = `🎵 音乐时间：${audioTime}s / ${totalTime}`;
    }

    // 播放按钮点击事件
    playButton.addEventListener("click", () => {
      if (audio.paused) {
        const parsed = parsePlayAtInput();
        if (!parsed) return alert("请输入合法时间");

        playAt = parsed;
        isPlaying = false; // 重置播放状态，让定时器重新判断是否该播放
      } else {
        audio.pause();
        playButton.textContent = "播放";
        isPlaying = false; // 暂停后重新判断播放时间
      }
    });

    // 偏移调整按钮
    adjustPlus.addEventListener("click", () => {
      localOffset += OFFSET_STEP;
    });

    adjustMinus.addEventListener("click", () => {
      localOffset -= OFFSET_STEP;
    });

    // 音频播放结束事件
    audio.addEventListener("ended", () => {
      playButton.textContent = "播放";
      isPlaying = false;
    });

    // 初始化设定播放时间
    setDefaultPlayAt();

    // 定时器：刷新状态 + 检查是否需要启动播放
    setInterval(() => {
      updateStatus();
      checkAndStartPlayback();
    }, 20); // 推荐间隔：10~50ms，根据性能调节