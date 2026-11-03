"use strict";

const currency = new Intl.NumberFormat("id-ID", {
  style: "currency",
  currency: "IDR",
  maximumFractionDigits: 0,
});

const profileMenu = document.querySelector(".profile-menu");
if (profileMenu) {
  document.addEventListener("click", (event) => {
    if (!profileMenu.contains(event.target)) profileMenu.open = false;
  });
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && profileMenu.open) {
      profileMenu.open = false;
      profileMenu.querySelector("summary").focus();
    }
  });
  profileMenu.addEventListener("focusout", (event) => {
    if (!profileMenu.contains(event.relatedTarget)) profileMenu.open = false;
  });
}

const dialog = document.querySelector("#cart-dialog");
const cartContent = document.querySelector("#cart-content");
let cartRequest;

async function openCart() {
  if (!dialog) return;
  cartRequest?.abort();
  cartRequest = new AbortController();
  const request = cartRequest;
  const loading = document.querySelector("#cart-loading");
  const feedback = document.querySelector("#cart-feedback");
  const errorText = document.querySelector("#cart-error-text");
  const pageError = document.querySelector(".page-error");
  feedback.hidden = !pageError;
  errorText.textContent = pageError?.textContent || "";
  loading.hidden = false;
  cartContent.replaceChildren();
  if (!dialog.open) dialog.showModal();
  try {
    const response = await fetch("/cart/panel", { signal: request.signal });
    if (response.redirected) {
      window.location.assign(response.url);
      return;
    }
    if (!response.ok)
      throw new Error("Keranjang belum dapat dimuat. Silakan coba lagi.");
    const documentFragment = new DOMParser().parseFromString(
      await response.text(),
      "text/html",
    );
    const panel = documentFragment.querySelector("[data-cart-panel]");
    if (!panel)
      throw new Error(
        "Keranjang belum dapat dimuat. Muat ulang halaman lalu coba lagi.",
      );
    if (!request.signal.aborted && dialog.open)
      cartContent.replaceChildren(panel);
  } catch (error) {
    if (error.name === "AbortError") return;
    errorText.textContent = error.message;
    feedback.hidden = false;
  } finally {
    if (cartRequest === request && !request.signal.aborted)
      loading.hidden = true;
  }
}

function closeCart() {
  cartRequest?.abort();
  dialog?.close();
}

if (dialog) {
  document.querySelector("#cart-toggle").hidden = false;
  document.querySelector("#cart-toggle").addEventListener("click", openCart);
  document.querySelector("#cart-close").addEventListener("click", closeCart);
  document.querySelector("#cart-retry").addEventListener("click", openCart);
  dialog.addEventListener("close", () => {
    cartRequest?.abort();
    if (document.activeElement === document.body)
      document.querySelector("#cart-toggle").focus();
  });
  dialog.addEventListener("click", (event) => {
    if (event.target !== dialog) return;
    const bounds = dialog.getBoundingClientRect();
    if (
      event.clientX < bounds.left ||
      event.clientX > bounds.right ||
      event.clientY < bounds.top ||
      event.clientY > bounds.bottom
    )
      closeCart();
  });
  document.addEventListener("click", (event) => {
    if (!event.target.closest("[data-open-cart]")) return;
    event.preventDefault();
    openCart();
  });
  const url = new URL(window.location.href);
  if (url.searchParams.get("cart") === "open") {
    openCart();
    url.searchParams.delete("cart");
    window.history.replaceState(null, "", url);
  }
}

document.addEventListener("change", (event) => {
  const input = event.target;
  if (!input.matches("[data-checkout] input[name=method]")) return;
  const checkout = input.closest("[data-checkout]");
  checkout.querySelector("[data-checkout-fee]").textContent = currency.format(
    input.dataset.fee,
  );
  checkout.querySelector("[data-checkout-total]").textContent = currency.format(
    input.dataset.total,
  );
});

const courseType = document.querySelector("#course-type");
function updateCourseFields() {
  document.querySelectorAll("[data-course-type]").forEach((fieldset) => {
    const inactive = fieldset.dataset.courseType !== courseType.value;
    fieldset.hidden = inactive;
    fieldset.disabled = inactive;
  });
}
if (courseType) {
  courseType.addEventListener("change", updateCourseFields);
  updateCourseFields();
}

const player = document.querySelector("#player");
if (player) {
  const videoError = document.querySelector("#video-error");
  player.addEventListener("error", () => {
    videoError.hidden = false;
  });
  document.querySelectorAll("[data-chapter]").forEach((button) => {
    button.addEventListener("click", async () => {
      document.querySelector("#chapter-title").textContent = button.textContent;
      videoError.hidden = true;
      try {
        player.currentTime = 0;
        await player.play();
      } catch (_) {
        videoError.hidden = false;
      }
    });
  });
}

document.addEventListener("submit", (event) => {
  const message = event.target.dataset.confirm;
  if (message && !window.confirm(message)) event.preventDefault();
});
