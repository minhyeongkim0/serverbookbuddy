const ChatManager = (function () {
  // === State ===
  const state = {
    stompClient: null,
    currentUser: null,
    activeRoomId: null,
    activePartnerId: null,
    isOpen: false,
    subscription: null,
    apiBaseUrl: "/api/chat",
  };

  // === Elements ===
  const el = {
    popup: null,
    fab: null,
    headerTitle: null,
    headerSubtitle: null,
    btnBack: null,
    viewList: null,
    listContainer: null,
    emptyListMsg: null,
    loadingList: null,
    viewRoom: null,
    messages: null,
    input: null,
  };

  // === Initialization ===
  function init() {
    el.popup = document.getElementById("chat-window");
    el.fab = document.getElementById("chat-fab");
    el.headerTitle = document.getElementById("header-title");
    el.headerSubtitle = document.getElementById("header-subtitle");
    el.btnBack = document.getElementById("btn-back");
    el.viewList = document.getElementById("view-list");
    el.listContainer = document.getElementById("room-list-container");
    el.emptyListMsg = document.getElementById("empty-list-msg");
    el.loadingList = document.getElementById("loading-list");
    el.viewRoom = document.getElementById("view-room");
    el.messages = document.getElementById("chat-messages");
    el.input = document.getElementById("chat-input");

    const configEl = document.getElementById("chat-config");
    if (configEl) {
      state.currentUser = {
        id: parseInt(configEl.dataset.userId),
        name: configEl.dataset.userName,
      };
      connectStomp();
    }
  }

  // === Connection ===
  function connectStomp() {
    if (state.stompClient && state.stompClient.connected) return;

    const socket = new SockJS("/ws-stomp");
    state.stompClient = Stomp.over(socket);
    state.stompClient.debug = null;

    state.stompClient.connect(
      {},
      function (frame) {
        console.log("✅ STOMP Connected");

        state.stompClient.subscribe(
          "/sub/member/" + state.currentUser.id,
          function () {
            if (state.isOpen) fetchRoomList();
          }
        );

        if (state.activeRoomId) subscribeToCurrentRoom();
      },
      function (error) {
        console.error("STOMP Error:", error);
        setTimeout(connectStomp, 5000);
      }
    );
  }

  function subscribeToCurrentRoom() {
    if (
      !state.stompClient ||
      !state.stompClient.connected ||
      !state.activeRoomId
    )
      return;
    if (state.subscription) state.subscription.unsubscribe();

    state.subscription = state.stompClient.subscribe(
      "/sub/chat/room/" + state.activeRoomId,
      function (messageOutput) {
        const data = JSON.parse(messageOutput.body);
        if (data.senderId !== state.currentUser.id) {
          appendMessage(data.senderName, data.content, false, data.sentAt);
        }
      }
    );
  }

  // === Actions ===

  function togglePopup() {
    if (!state.currentUser) {
      if (confirm("로그인이 필요한 서비스입니다.\n로그인 하시겠습니까?"))
        location.href = "/login";
      return;
    }

    state.isOpen = !state.isOpen;
    if (state.isOpen) {
      el.popup.classList.remove("chat-hidden");
      el.popup.classList.add("chat-visible");
      if (!state.activeRoomId) showList();
    } else {
      el.popup.classList.remove("chat-visible");
      el.popup.classList.add("chat-hidden");
    }
  }

  // 채팅방 생성 및 시작 (상세페이지 버튼용)
  async function startChat(partnerId, bookId, partnerName) {
    if (!state.currentUser) {
      if (confirm("로그인이 필요합니다.")) location.href = "/login";
      return;
    }

    try {
      // 1. 방 생성 요청
      const response = await axios.post(`${state.apiBaseUrl}/room`, {
        senderId: state.currentUser.id,
        receiverId: partnerId,
        bookId: bookId,
      });

      const roomId = String(response.data);

      // 2. 팝업 열고 해당 방으로 진입
      if (!state.isOpen) togglePopup();
      openDirectChat(roomId, partnerName, partnerId);
    } catch (error) {
      console.error("채팅방 생성 실패:", error);
      alert("채팅방을 열 수 없습니다.");
    }
  }

  async function showList() {
    if (state.subscription) {
      state.subscription.unsubscribe();
      state.subscription = null;
    }
    state.activeRoomId = null;
    state.activePartnerId = null;

    el.headerTitle.innerText = "채팅 목록";
    el.headerSubtitle.classList.add("d-none");
    el.btnBack.classList.add("d-none");

    el.viewRoom.classList.remove("d-flex");
    el.viewRoom.classList.add("d-none");

    el.viewList.classList.remove("d-none");

    await fetchRoomList();
  }

  async function fetchRoomList() {
    el.listContainer.innerHTML = "";
    el.loadingList.classList.remove("d-none");
    el.emptyListMsg.classList.add("d-none");

    try {
      const res = await axios.get(`${state.apiBaseUrl}/rooms`);
      const rooms = res.data;
      el.loadingList.classList.add("d-none");

      if (!rooms || rooms.length === 0) {
        el.emptyListMsg.classList.remove("d-none");
        el.emptyListMsg.style.display = "flex";
        return;
      }

      rooms.forEach((room) => {
        const item = document.createElement("div");
        item.className = "room-item";
        // 목록 클릭 시 파트너 ID는 null (기존 대화방이므로 메시지 수신 시 처리됨)
        item.onclick = () =>
          openDirectChat(room.roomId, room.roomName, room.partnerId);

        item.innerHTML = `
                    <div class="room-icon"><i class="fas fa-user"></i></div>
                    <div class="room-info">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <span class="room-title">${room.roomName}</span>
                            <span class="room-status">${room.status}</span>
                        </div>
                        <div class="room-last-msg">${room.lastMessage}</div>
                    </div>
                `;
        el.listContainer.appendChild(item);
      });
    } catch (err) {
      console.error(err);
      el.loadingList.classList.add("d-none");
    }
  }

  async function openDirectChat(roomId, roomName, partnerId) {
    state.activeRoomId = roomId;
    state.activePartnerId = partnerId;

    el.headerTitle.innerText = roomName;
    el.headerSubtitle.classList.remove("d-none");
    el.btnBack.classList.remove("d-none");

    el.viewList.classList.add("d-none");

    el.viewRoom.classList.remove("d-none");
    el.viewRoom.classList.add("d-flex");

    if (state.stompClient && state.stompClient.connected)
      subscribeToCurrentRoom();
    else connectStomp();

    el.messages.innerHTML = "";
    try {
      const res = await axios.get(`${state.apiBaseUrl}/history/${roomId}`);
      res.data.forEach((msg) => {
        appendMessage(
          msg.senderName,
          msg.content,
          msg.senderId === state.currentUser.id,
          msg.sentAt
        );
      });
      el.messages.scrollTop = el.messages.scrollHeight;
    } catch (err) {
      console.error(err);
    }
  }

  function sendMessage() {
    const content = el.input.value.trim();
    if (!content || !state.activeRoomId) return;

    if (!state.stompClient || !state.stompClient.connected) {
      alert("연결 끊김. 재연결 중...");
      connectStomp();
      return;
    }

    const payload = {
      roomId: state.activeRoomId,
      senderId: state.currentUser.id,
      senderName: state.currentUser.name,
      receiverId: state.activePartnerId,
      content: content,
    };

    state.stompClient.send("/pub/chat/message", {}, JSON.stringify(payload));
    appendMessage(
      state.currentUser.name,
      content,
      true,
      new Date().toISOString()
    );

    el.input.value = "";
    el.input.style.height = "24px";
    el.input.focus();
  }

  function appendMessage(sender, text, isMe, timeStr) {
    const time = timeStr
      ? new Date(timeStr).toLocaleTimeString([], {
          hour: "2-digit",
          minute: "2-digit",
        })
      : "방금";
    const wrapper = document.createElement("div");
    wrapper.className = `msg-row ${isMe ? "me" : "other"}`;
    wrapper.innerHTML = `<div class="msg-bubble">${text}</div><span class="msg-time">${time}</span>`;

    el.messages.appendChild(wrapper);
    el.messages.scrollTop = el.messages.scrollHeight;
  }

  document.addEventListener("DOMContentLoaded", init);

  return { togglePopup, openDirectChat, showList, sendMessage, startChat };
})();
