<template>
  <div class="nau-impression">
    <!-- 顶部轮播图 -->
    <div class="carousel-section">
      <el-carousel :interval="4000" arrow="always" height="80vh" :autoplay="true">
        <el-carousel-item v-for="(banner, index) in banners" :key="index">
          <div class="carousel-item" :style="{ backgroundImage: `url(${banner.url})` }">
            <div class="carousel-overlay"></div>
            <div class="carousel-content">
              <h1 class="carousel-title">{{ banner.title }}</h1>
              <p class="carousel-description">{{ banner.description }}</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 南审简介 -->
    <section class="introduction-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">南京审计大学简介</h2>
          <div class="divider">
            <span class="divider-line"></span>
            <span class="divider-icon">●</span>
            <span class="divider-line"></span>
          </div>
        </div>
        
        <div class="intro-content">
          <div class="intro-text">
            <p>南京审计大学（Nanjing Audit University）位于江苏省南京市，是中华人民共和国审计署直属的一所以经济学、管理学、法学为主干学科，文学、理学、工学、艺术学等多学科协调发展的高等学校。</p>
            <p>学校始建于1983年，前身为国家审计署南京审计学院，2015年更名为南京审计大学。作为我国唯一一所审计特色高校，学校秉承"明德至诚、博学笃行"的校训，培养了大批优秀的审计、金融和管理人才。</p>
            <p>学校拥有现代化的教学设施和优美的校园环境，校园内绿树成荫、湖光潋滟，是莘莘学子求学的理想之地。以下是南京审计大学的美丽风光，展现这所充满活力与魅力的高等学府。</p>
            <el-button type="primary" class="more-btn" @click="scrollToGallery">探索南审风光</el-button>
          </div>
          <div class="intro-image">
            <img src="/src/assets/pics/校园风景3.png" alt="南京审计大学" class="intro-img">
            <div class="intro-image-caption">美丽的南审校园</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 校园风光展示 -->
    <section id="gallery-section" class="gallery-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">校园风光</h2>
          <div class="divider">
            <span class="divider-line"></span>
            <span class="divider-icon">●</span>
            <span class="divider-line"></span>
          </div>
          <p class="section-subtitle">感受南审四季之美，领略校园独特魅力</p>
        </div>

        <div class="gallery-filters">
          <el-radio-group v-model="activeFilter" @change="handleFilterChange">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="architecture">校园建筑</el-radio-button>
            <el-radio-button label="seasonal">四季变换</el-radio-button>
            <el-radio-button label="campus">校园风景</el-radio-button>
            <el-radio-button label="life">校园生活</el-radio-button>
          </el-radio-group>
        </div>

        <div class="gallery-grid" ref="galleryGrid">
          <div 
            v-for="(image, index) in displayedImages" 
            :key="index"
            class="gallery-item"
            @click="openPhotoViewer(index)"
          >
            <div class="gallery-image-wrapper">
              <img 
                :src="image.url" 
                :alt="image.title" 
                class="gallery-image"
                loading="lazy"
              >
              <div class="gallery-caption">
                <h3>{{ image.title }}</h3>
                <p>{{ image.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 学校特色介绍 -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">南审特色</h2>
          <div class="divider">
            <span class="divider-line"></span>
            <span class="divider-icon">●</span>
            <span class="divider-line"></span>
          </div>
        </div>
        
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><School /></el-icon>
            </div>
            <h3>一流学科</h3>
            <p>拥有审计学、会计学等特色学科，培养高素质专业人才</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><HomeFilled /></el-icon>
            </div>
            <h3>优美环境</h3>
            <p>校园环境优美，绿树成荫，是学习生活的理想场所</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <h3>国际交流</h3>
            <p>与多所国际知名高校建立合作关系，开展广泛学术交流</p>
          </div>
          
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <h3>就业前景</h3>
            <p>毕业生就业率高，在各行各业表现优异，深受用人单位好评</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 校园生活剪影 -->
    <section class="campus-life-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">校园生活剪影</h2>
          <div class="divider">
            <span class="divider-line"></span>
            <span class="divider-icon">●</span>
            <span class="divider-line"></span>
          </div>
          <p class="section-subtitle">丰富多彩的南审校园生活</p>
        </div>
        
        <div class="life-highlights">
          <div class="highlight-item" v-for="(highlight, index) in campusLifeHighlights" :key="index" @click="openHighlightViewer(highlight)">
            <div class="highlight-image" :style="{ backgroundImage: `url(${highlight.image})` }"></div>
            <div class="highlight-info">
              <h3>{{ highlight.title }}</h3>
              <p>{{ highlight.description }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 照片查看器 -->
    <el-dialog
      v-model="photoViewerVisible"
      fullscreen
      :show-close="true"
      class="photo-viewer-dialog"
    >
      <div class="photo-viewer">
        <div class="viewer-header">
          <h2>{{ currentImage?.title }}</h2>
          <div class="viewer-controls">
            <el-button @click="photoViewerVisible = false" circle>
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>
        
        <div class="viewer-content">
          <el-button 
            v-if="currentImageIndex > 0" 
            class="nav-btn prev-btn" 
            circle 
            @click="prevImage"
          >
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          
          <div class="main-image-container">
            <img :src="currentImage?.url" :alt="currentImage?.title" class="main-image">
            <div class="image-description">
              <p>{{ currentImage?.description }}</p>
              <div class="image-meta" v-if="currentImage?.location">
                <span class="image-location">拍摄地点: {{ currentImage?.location }}</span>
              </div>
            </div>
          </div>
          
          <el-button 
            v-if="currentImageIndex < displayedImages.length - 1" 
            class="nav-btn next-btn" 
            circle 
            @click="nextImage"
          >
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from 'vue'
import { Close, ArrowLeft, ArrowRight, School, HomeFilled, Connection, OfficeBuilding } from '@element-plus/icons-vue'

// 轮播图数据
const banners = [
  {
    url: '/src/assets/pics/校园风景1.jpg',
    title: '南审印象',
    description: '发现南京审计大学的美丽与魅力'
  },
  {
    url: '/src/assets/pics/校园风景4.jpg',
    title: '知识殿堂',
    description: '培育专业审计人才的摇篮'
  },
  {
    url: '/src/assets/pics/校园风景16.jpg',
    title: '美丽校园',
    description: '四季如画的南审校园风光'
  },
  {
    url: '/src/assets/pics/校园风景8.jpg',
    title: '青春活力',
    description: '充满活力的南审学子'
  }
]

// 校园生活亮点
const campusLifeHighlights = [
  {
    title: '学术交流',
    description: '学校定期举办学术讲座与研讨会，丰富学生的学术视野',
    image: '/src/assets/pics/校园风景15.png'
  },
  {
    title: '文化艺术节',
    description: '丰富多彩的校园文化活动，展现南审学子的青春风采',
    image: '/src/assets/pics/校园风景8.jpg'
  },
  {
    title: '运动会',
    description: '年度运动会是展示体育精神和团队合作的重要平台',
    image: '/src/assets/pics/校园风景21.png'
  },
  {
    title: '社团活动',
    description: '各式各样的学生社团为校园生活增添丰富多彩的色彩',
    image: '/src/assets/pics/校园风景17.jpg'
  }
]

// 图片数据接口
interface NAUImage {
  id: number
  url: string
  title: string
  description: string
  category: string
  location?: string
}

// 图片数据
const images = ref<NAUImage[]>([
  {
    id: 1,
    url: '/src/assets/pics/校园风景1.jpg',
    title: '图书馆全景',
    description: '南审图书馆全景，知识的殿堂',
    category: 'architecture',
    location: '图书馆'
  },
  {
    id: 2,
    url: '/src/assets/pics/校园风景2.jpg',
    title: '校园樱花季',
    description: '春天的南审，樱花盛开的美丽景色',
    category: 'seasonal',
    location: '校园主道'
  },
  {
    id: 3,
    url: '/src/assets/pics/校园风景3.png',
    title: '湖畔晨曦',
    description: '清晨的校园湖，波光粼粼',
    category: 'campus',
    location: '校园湖'
  },
  {
    id: 4,
    url: '/src/assets/pics/校园风景4.jpg',
    title: '主教学楼',
    description: '南审的主教学楼，学习的场所',
    category: 'architecture',
    location: '主教学楼'
  },
  {
    id: 5,
    url: '/src/assets/pics/校园风景5.jpg',
    title: '校园生活一角',
    description: '学生们在校园草坪上学习交流的日常场景',
    category: 'life',
    location: '中央草坪'
  },
  {
    id: 6,
    url: '/src/assets/pics/校园风景6.jpg',
    title: '南审秋色',
    description: '秋天的校园，落叶缤纷，色彩斑斓',
    category: 'seasonal',
    location: '校园林荫道'
  },
  {
    id: 7,
    url: '/src/assets/pics/校园风景7.jpg',
    title: '晨曦中的校园',
    description: '清晨的校园，被朝阳唤醒的宁静美景',
    category: 'campus',
    location: '主校区'
  },
  {
    id: 8,
    url: '/src/assets/pics/校园风景8.jpg',
    title: '文化艺术节',
    description: '南审学子在文化艺术节上展示才艺风采',
    category: 'life',
    location: '艺术中心'
  },
  {
    id: 9,
    url: '/src/assets/pics/校园风景9.jpg',
    title: '宿舍区晚霞',
    description: '夕阳下的学生宿舍，温暖而安静',
    category: 'architecture',
    location: '学生宿舍区'
  },
  {
    id: 10,
    url: '/src/assets/pics/校园风景10.jpg',
    title: '青春时光',
    description: '学生们在操场上奔跑、锻炼的活力场景',
    category: 'life',
    location: '运动场'
  },
  {
    id: 11,
    url: '/src/assets/pics/校园风景11.jpg',
    title: '图书馆内景',
    description: '图书馆内部，书籍与知识的海洋',
    category: 'architecture',
    location: '图书馆'
  },
  {
    id: 12,
    url: '/src/assets/pics/校园风景12.jpg',
    title: '校园雪景',
    description: '银装素裹的校园，宛如童话世界',
    category: 'seasonal',
    location: '校园主道'
  }
])

// 过滤器状态
const activeFilter = ref('all')

// 显示的图片
const displayedImages = computed(() => {
  if (activeFilter.value === 'all') {
    return images.value
  } else {
    return images.value.filter(img => img.category === activeFilter.value)
  }
})

// 照片查看器
const photoViewerVisible = ref(false)
const currentImageIndex = ref(0)
const currentImage = computed(() => {
  return displayedImages.value[currentImageIndex.value] || null
})

// 打开照片查看器
const openPhotoViewer = (index: number) => {
  currentImageIndex.value = index
  photoViewerVisible.value = true
}

// 查看下一张图片
const nextImage = () => {
  if (currentImageIndex.value < displayedImages.value.length - 1) {
    currentImageIndex.value++
  }
}

// 查看上一张图片
const prevImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

// 打开高亮内容查看器
const openHighlightViewer = (highlight: any) => {
  // 这里可以添加查看校园生活亮点的逻辑
  console.log('查看校园生活亮点:', highlight.title)
}

// 处理过滤器变更
const handleFilterChange = () => {
  // 可以添加额外的动画效果
}

// 滚动到图片展示区
const scrollToGallery = () => {
  document.getElementById('gallery-section')?.scrollIntoView({
    behavior: 'smooth'
  })
}

// 组件挂载时
onMounted(() => {
  // 可以在这里添加其他初始化逻辑
  // 例如：图片懒加载、动画效果等
})
</script>

<style lang="scss" scoped>
// 主容器样式
.nau-impression {
  color: #333;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  overflow-x: hidden;
}

// 容器通用样式
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

// 段落样式
p {
  line-height: 1.8;
  margin: 0 0 15px;
}

// 分割线样式
.divider {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 15px 0 30px;
  
  .divider-line {
    height: 1px;
    width: 100px;
    background-color: #ddd;
  }
  
  .divider-icon {
    margin: 0 15px;
    color: #1890ff;
    font-size: 12px;
  }
}

// 部分标题样式
.section-header {
  text-align: center;
  margin-bottom: 40px;
  
  .section-title {
    font-size: 36px;
    font-weight: 700;
    margin: 0;
    color: #333;
  }
  
  .section-subtitle {
    font-size: 18px;
    color: #666;
    margin-top: 10px;
  }
}

// 轮播图部分
.carousel-section {
  :deep(.el-carousel__indicators) {
    z-index: 10;
  }
  
  :deep(.el-carousel__arrow) {
    background-color: rgba(0, 0, 0, 0.3);
    font-size: 20px;
    width: 50px;
    height: 50px;
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.5);
    }
  }
  
  .carousel-item {
    height: 100%;
    background-size: cover;
    background-position: center;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  
  .carousel-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.3);
    z-index: 1;
  }
  
  .carousel-content {
    position: relative;
    color: white;
    z-index: 2;
    text-align: center;
    padding: 0 20px;
    max-width: 800px;
  }
  
  .carousel-title {
    font-size: 4.5rem;
    font-weight: 700;
    margin-bottom: 20px;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.7);
    
    @media (max-width: 768px) {
      font-size: 3rem;
    }
  }
  
  .carousel-description {
    font-size: 1.8rem;
    font-weight: 300;
    text-shadow: 0 2px 10px rgba(0, 0, 0, 0.7);
    
    @media (max-width: 768px) {
      font-size: 1.4rem;
    }
  }
}

// 简介部分
.introduction-section {
  padding: 80px 0;
  background-color: #fff;
  
  .intro-content {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 40px;
    
    .intro-text {
      flex: 1;
      min-width: 300px;
      
      .more-btn {
        margin-top: 20px;
      }
    }
    
    .intro-image {
      flex: 1;
      min-width: 300px;
      position: relative;
      
      .intro-img {
        width: 100%;
        border-radius: 8px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        transition: transform 0.3s ease;
        
        &:hover {
          transform: scale(1.02);
        }
      }
      
      .intro-image-caption {
        position: absolute;
        bottom: 20px;
        right: 20px;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        padding: 8px 15px;
        border-radius: 20px;
        font-size: 14px;
      }
    }
  }
}

// 图片展示部分
.gallery-section {
  padding: 80px 0;
  background-color: #f8f9fa;
  
  .gallery-filters {
    display: flex;
    justify-content: center;
    margin-bottom: 40px;
    
    .el-radio-group {
      background: white;
      border-radius: 30px;
      padding: 5px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    }
    
    :deep(.el-radio-button__inner) {
      padding: 10px 20px;
      border: none;
      background: transparent;
      transition: all 0.3s;
      border-radius: 25px;
      
      &:hover {
        color: #1890ff;
      }
    }
    
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      background-color: #1890ff;
      box-shadow: 0 5px 15px rgba(24, 144, 255, 0.3);
      transform: translateY(-2px);
    }
  }
  
  .gallery-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 25px;
    
    .gallery-item {
      cursor: pointer;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      transition: all 0.3s cubic-bezier(0.165, 0.84, 0.44, 1);
      background: white;
      
      &:hover {
        transform: translateY(-10px);
        box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
        
        .gallery-image {
          transform: scale(1.05);
        }
        
        .gallery-caption {
          opacity: 1;
        }
      }
      
      .gallery-image-wrapper {
        position: relative;
        overflow: hidden;
        height: 220px;
      }
      
      .gallery-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: transform 0.5s ease;
      }
      
      .gallery-caption {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
        color: white;
        padding: 20px;
        opacity: 0;
        transition: opacity 0.3s ease;
        
        h3 {
          margin: 0 0 5px;
          font-size: 20px;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          font-size: 14px;
          opacity: 0.9;
        }
      }
    }
  }
}

// 特色部分
.features-section {
  padding: 80px 0;
  background-color: #fff;
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 30px;
    
    .feature-card {
      background: white;
      border-radius: 8px;
      padding: 30px;
      text-align: center;
      transition: all 0.3s ease;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
      
      &:hover {
        transform: translateY(-10px);
        box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
        
        .feature-icon {
          background-color: #1890ff;
          color: white;
          transform: rotateY(180deg);
        }
      }
      
      .feature-icon {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background-color: #f1f8ff;
        color: #1890ff;
        display: flex;
        align-items: center;
        justify-content: center;
        margin: 0 auto 20px;
        transition: all 0.5s ease;
        
        .el-icon {
          font-size: 36px;
        }
      }
      
      h3 {
        margin: 0 0 15px;
        font-size: 20px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        color: #666;
        font-size: 16px;
        line-height: 1.6;
      }
    }
  }
}

// 校园生活剪影
.campus-life-section {
  padding: 80px 0;
  background-color: #f8f9fa;
  
  .life-highlights {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 30px;
    
    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
    
    .highlight-item {
      display: flex;
      background: white;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-10px);
        box-shadow: 0 15px 30px rgba(0, 0, 0, 0.15);
        
        .highlight-image {
          transform: scale(1.05);
        }
      }
      
      .highlight-image {
        flex: 1;
        min-width: 180px;
        background-size: cover;
        background-position: center;
        transition: transform 0.5s ease;
      }
      
      .highlight-info {
        flex: 1;
        padding: 20px;
        
        h3 {
          margin: 0 0 10px;
          font-size: 20px;
          font-weight: 600;
        }
        
        p {
          margin: 0;
          font-size: 14px;
          color: #666;
          line-height: 1.6;
        }
      }
    }
  }
}

// 照片查看器
.photo-viewer-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }
  
  :deep(.el-dialog__body) {
    padding: 0;
  }
}

.photo-viewer {
  width: 100%;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.95);
  color: white;
  display: flex;
  flex-direction: column;
  
  .viewer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    background-color: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(10px);
    
    h2 {
      margin: 0;
      font-size: 1.8rem;
      font-weight: 500;
    }
    
    .viewer-controls {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }
  }
  
  .viewer-content {
    flex: 1;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 2rem;
    position: relative;
    
    .nav-btn {
      background-color: rgba(255, 255, 255, 0.1);
      border: none;
      width: 60px;
      height: 60px;
      z-index: 2;
      backdrop-filter: blur(5px);
      transition: all 0.3s ease;
      
      &:hover {
        background-color: rgba(255, 255, 255, 0.2);
        transform: scale(1.1);
      }
      
      .el-icon {
        font-size: 26px;
        color: white;
      }
    }
    
    .main-image-container {
      flex: 1;
      height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      margin: 0 2rem;
      position: relative;
      
      .main-image {
        max-width: 100%;
        max-height: calc(100vh - 300px);
        object-fit: contain;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        border-radius: 4px;
        transition: transform 0.5s cubic-bezier(0.165, 0.84, 0.44, 1);
      }
      
      .image-description {
        margin-top: 2rem;
        text-align: center;
        max-width: 800px;
        
        p {
          font-size: 1.2rem;
          margin-bottom: 1rem;
          line-height: 1.6;
        }
        
        .image-meta {
          display: flex;
          justify-content: center;
          gap: 2rem;
          font-size: 1rem;
          color: rgba(255, 255, 255, 0.7);
          
          @media (max-width: 768px) {
            flex-direction: column;
            gap: 0.5rem;
          }
        }
      }
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .section-header .section-title {
    font-size: 28px;
  }
  
  .gallery-section .gallery-filters {
    overflow-x: auto;
    padding-bottom: 15px;
    
    &::-webkit-scrollbar {
      height: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background-color: rgba(0, 0, 0, 0.1);
      border-radius: 2px;
    }
    
    .el-radio-group {
      flex-wrap: nowrap;
      width: max-content;
    }
  }
  
  .photo-viewer {
    .viewer-header h2 {
      font-size: 1.5rem;
    }
    
    .viewer-content {
      padding: 1rem;
      
      .nav-btn {
        width: 50px;
        height: 50px;
        
        .el-icon {
          font-size: 22px;
        }
      }
      
      .main-image-container {
        margin: 0 1rem;
        
        .image-description p {
          font-size: 1rem;
        }
      }
    }
  }
}
</style> 