<script setup>
import LoginFrame from '@/components/LoginBox.vue'
</script>

<template>
  <div class="canvas">
    <LoginFrame></LoginFrame>
  </div>
</template>

<style scoped>
.canvas {
  min-height: 100vh;
  padding: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>