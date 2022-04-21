# 연합학습 기반 피부질환 이미지 분류 안드로이드 앱


* **개발기간** : 21.08.18 ~ 21.12.31

* **개발자** : [hyeonjin](https://github.com/ssonghj)

* **개발환경** 

  📌 IDE : Jupyter notebook, Android Studio

  📌 Programming Language : Python, Java

  📌 Application UI Toolkit : Android

## 프로젝트 설명

그림1과 같이 연합학습 구조 및 피부 질환 이미지 분류 과정을 구현했다.</br>
본 구조는 모바일 클라이언트와 연합학습 서버로 구성된다.</br>
모바일 클라이언트는 기기 내부에서의 학습과 피부 질환 이미지 분류가 이루어진다.</br>
연합학습 서버는 Flower1의 서버로 모바일 클라이언트 학습 결과들의 가중치를 집계하여 새로운 모델을 만든 후 재전송하게 된다.</br>
이 구조는 모바일 기기에서 학습 결과의 가중치만 Flower 서버로 이동하기에 개인 정보의 외부 노출이 없다.</br>

![스크린샷 2022-04-21 오후 6 34 28](https://user-images.githubusercontent.com/40493508/164426143-d7aac568-2b38-4f69-bb35-0bf99e61dbaf.png)
