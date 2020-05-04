FROM node:latest

COPY . /frontend
WORKDIR /frontend

RUN yarn install
RUN yarn global add serve
RUN yarn build

EXPOSE 4000
CMD serve -s build -l 4000
