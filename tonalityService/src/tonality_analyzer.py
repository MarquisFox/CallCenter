from dostoevsky.tokenization import RegexTokenizer
from dostoevsky.models import FastTextSocialNetworkModel
import config

class TonalityAnalyzer:
    def __init__(self):
        FastTextSocialNetworkModel.MODEL_PATH = config.MODEL_PATH
        self.tokenizer = RegexTokenizer()
        self.model = FastTextSocialNetworkModel(tokenizer=self.tokenizer)

    def analyze(self, text) -> str:
        if not text or not text.strip():
            raise ValueError("Пустой текст для анализа тональности")
        result = self.model.predict([text], k=1)
        sentiment = list(result[0].keys())[0]
        return sentiment