### 对抗网络GAN生成图片之微信插件实现

大家好，我是奇点。

本周在朋友圈里，有一个叫云萌宠的小产品，在微信公众号里回复”撸猫“或”撸狗“，微信公众号就会回复一张机器生成”不存在“的图片。上线不久，直接把服务器给撸挂了。接下来，我们讲一下微信插件的代码实现。

本插件是基于于开源建站工具JPress开发的，JPress的插件可以在线安装或卸载，使用起来非常方便。接下来，我们直接进入正题，代码集成过程。



> 核心代码

如果还对GAN或云萌宠不太了解的读者，可以简单回顾一下奇点之前写的文章，在这里不再展开讲解。为了更好地集成和使用，我们对之前的代码进行简单地改造，分成加载模型和预测两部分。具体代码如下：



~~~java
public class GanKit {
	private static Log log = Log.getLog(GanKit.class);

	public static GanKit me = new GanKit();

	private Criteria<int[], Image[]> criteria;
	private ZooModel<int[], Image[]> model;

	private GanKit() {
	}

	public synchronized Criteria<int[], Image[]> gan() {
		if (criteria == null) {
			criteria = Criteria.builder().
					optApplication(Application.CV.IMAGE_GENERATION)
					.setTypes(int[].class, Image[].class)
					.optFilter("size", "256")
					.optArgument("truncation", 0.4f)
					.optProgress(new ProgressBar())
					.optEngine(PtEngine.ENGINE_NAME).build();
		}
		try {
			model = criteria.loadModel();
			log.info("load model succ.");
		} catch (ModelNotFoundException | MalformedModelException | IOException e) {
			log.error("load model error.", e);
		}
		return criteria;
	}

	public synchronized File generate(int i, String fileFix) {
		String attachmentRoot = JPressConfig.me.getAttachmentRootOrWebRoot();
		String path = attachmentRoot + "/gan/" + fileFix + "_" + StrKit.getRandomUUID() + ".png";

		log.debug("gen file path:{}.", path);

		File file = new File(path);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}

		FileOutputStream outputStream = null;
		try {
			int[] input = { i };
			Predictor<int[], Image[]> generator = model.newPredictor();
			Image[] out = generator.predict(input);
			outputStream = new FileOutputStream(file);
			out[0].save(outputStream, "png");
		} catch (Exception e) {
			log.error("generate image error.", e);
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}

		return file;
	}
}
~~~



> 开发JPress插件

新创建一个Maven Module，比如我创建了一个叫sinka-addons-djl的Module，继承插件AddonBase抽象类，在插件启动时，启动GAN模型加载。具体代码如下：

~~~java
@Override
public void onStart(AddonInfo addonInfo) {
    log.info("DjlAddons start");
    GanKit.me.gan();
}
~~~



> 开发微信插件

在这里，我们定义，只有在微信公众号里回复”撸猫“或”撸狗“时，才会生成图片。创建一个类继承WechatAddon接口。具体代码如下：

~~~java
@WechatAddonConfig(id = "com.sama.sinka.djl.wechat.DjlWechatAddon", 
                   title = "Djl Wechat Addon", 
                   description = "微信云萌宠插件", 
                   author = "奇点")
public class DjlWechatAddon implements WechatAddon {
}
~~~

ImageNet中，有很多喵星人和汪星人的分类，为了每次生成一张图片，所以，我们从这些分类中随机查找一个分类，然后让GAN生成该类型的图片，我们在这里做一下随机函数。

~~~java
private static Map<String, int[]> map = new HashMap<>();
private static Random random = new Random();
static {
    int[] cat = { 281, 283, 284, 285 };
    int[] dog = { 229, 230, 232, 200, 207 };
    map.put("撸猫", cat);
    map.put("撸狗", dog);
}
~~~

接下来简单微信公众号里”撸猫“或”撸狗“的文本消息进行筛选：

~~~java
@Override
public boolean onMatchingMessage(InMsg inMsg, MsgController msgController) {
    if (!(inMsg instanceof InTextMsg)) {
        return false;
    }

    InTextMsg inTextMsg = (InTextMsg) inMsg;
    String content = inTextMsg.getContent();
    return content != null && map.containsKey(content);
}
~~~

对于符合条件的消息进行做相应的生成图片处理：

~~~java
@Override
public boolean onRenderMessage(InMsg inMsg, MsgController msgController) {
    InTextMsg inTextMsg = (InTextMsg) inMsg;
    String content = inTextMsg.getContent();

    int[] range = map.get(content);
    int i = range[random.nextInt(range.length)];
    // 生成图片并上传到微信
    File file = GanKit.me.generate(i, inTextMsg.getFromUserName());
    ApiResult api = MediaApi.uploadMedia(MediaType.IMAGE, file);

    String mediaId = api.get("media_id");
    OutImageMsg imageMsg = new OutImageMsg(inMsg);
    imageMsg.setMediaId(mediaId);//发送图片
    msgController.render(imageMsg);
    return true;
}
~~~

到此，我们已经完成了云萌虫的所有代码，打包部署，即可体验神秘的深度学习GAN生成图片。



> 运行效果



你也可以在公众号里回复【003】获取我已经打包好的JPress插件，体验一下云萌宠的好玩之处。

<img src="https://www.d2lcoder.com/wechat.jpg"  width="260px" alt="动手学深度学习公众号"  align="left" />