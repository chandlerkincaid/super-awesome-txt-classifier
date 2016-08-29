package com.snakesinthebox.preprocessing

/**
  * @author Brad Bazemore
  *
  *         =Overview=
  *         Will take two text files, the training data and the stop words.
  *         The stop words have to be converted to sets and then distributed out to the nodes to
  *         prevent redundant shuffling of the data.
  *
  * 1. Convert doc into one RDD with each word as an element
  * 2. Remove all numbers and words with numbers in them
  * 3. Remove the odd special words such as &quote;
  * 4. Remove forward slashes and replace with a space
  * 5. Remove punctuations
  * 6. Convert all words to lowercase
  * 7. Remove stop words
  */

import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Driver object
  */
object Main {

  /**
    * Driver method
    *
    * @note This is for testing the preprocessing and will need to be moved elsewhere on deploy
    * @param args commandline argument to driver
    */
  def main(args: Array[String]): Unit = {

    val conf = ConfigFactory.load()

    val sparkConf = new SparkConf().setAppName(conf.getString("spark.appName"))
    val sc = new SparkContext(sparkConf)

    val trainData = sc.textFile(conf.getString("data.train.doc.path"))
    val categories = sc.textFile(conf.getString("data.train.cat.path"))
    val stopWords = sc.textFile(conf.getString("data.stopwords.path"))

    val stopWordsSet = stopWords.collect.toSet
    val stopWordsBC = sc.broadcast(stopWordsSet)

    val catData = categories.zip(trainData)

    val cData = catData
      .filter({case (key,value)=>key.contains("CCAT")})
      .values
      .flatMap(word => word.split(" "))
      .filter(Preprocessor.removeNumbers)
      .map(Preprocessor.removeSpecials)
      .map(Preprocessor.removeForwardSlash)
      .map(Preprocessor.removePunctuation)
      .map(word => word.toLowerCase())

    val gData = catData
      .filter({case (key,value)=>key.contains("GCAT")})
      .values
      .flatMap(word => word.split(" "))
      .filter(Preprocessor.removeNumbers)
      .map(Preprocessor.removeSpecials)
      .map(Preprocessor.removeForwardSlash)
      .map(Preprocessor.removePunctuation)
      .map(word => word.toLowerCase())

    val mData = catData
      .filter({case (key,value)=>key.contains("MCAT")})
      .values
      .flatMap(word => word.split(" "))
      .filter(Preprocessor.removeNumbers)
      .map(Preprocessor.removeSpecials)
      .map(Preprocessor.removeForwardSlash)
      .map(Preprocessor.removePunctuation)
      .map(word => word.toLowerCase())

    val eData = catData
      .filter({case (key,value)=>key.contains("ECAT")})
      .values
      .flatMap(word => word.split(" "))
      .filter(Preprocessor.removeNumbers)
      .map(Preprocessor.removeSpecials)
      .map(Preprocessor.removeForwardSlash)
      .map(Preprocessor.removePunctuation)
      .map(word => word.toLowerCase())

    val cClean = cData.mapPartitions {
      partition =>
        val stopWordsSet = stopWordsBC.value
        partition.filter(word => !stopWordsSet.contains(word))
    }
    val cWordCount = cClean
      .map(word=>(word,1))
      .reduceByKey(_ + _)


    val gClean = gData.mapPartitions {
      partition =>
        val stopWordsSet = stopWordsBC.value
        partition.filter(word => !stopWordsSet.contains(word))
    }
    val gWordCount = gClean
      .map(word=>(word,1))
      .reduceByKey(_ + _)


    val mClean = mData.mapPartitions {
      partition =>
        val stopWordsSet = stopWordsBC.value
        partition.filter(word => !stopWordsSet.contains(word))
    }
    val mWordCount = mClean
      .map(word=>(word,1))
      .reduceByKey(_ + _)


    val eClean = eData.mapPartitions {
      partition =>
        val stopWordsSet = stopWordsBC.value
        partition.filter(word => !stopWordsSet.contains(word))
    }
    val eWordCount = eClean
      .map(word=>(word,1))
      .reduceByKey(_ + _)

    val docTotal = (cWordCount++gWordCount++mWordCount++eWordCount)
      .reduceByKey(_ + _)

    println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
    cWordCount.take(10).foreach(println)
    println("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG")
    gWordCount.take(10).foreach(println)
    println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM")
    mWordCount.take(10).foreach(println)
    println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE")
    eWordCount.take(10).foreach(println)

    docTotal.take(10).foreach(println)


  }
}
