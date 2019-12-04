/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.anywide.dawdler.util;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.crypto.Cipher;
/**
 * 
 * @Title:  CertificateOperator.java
 * @Description:    证书验证的类 ，直接下面注释中的命令生成证书配置即可   
 * @author: jackson.song    
 * @date:   2015年05月23日    
 * @version V1.0 
 * @email: suxuan696@gmail.com
 */
public class CertificateOperator {
/**
 * 
 	keytool -validity 65535 -genkey -v -alias srchen -keyalg RSA -keystore dawdler.keystore -dname "CN=songrouchen,OU=互联网事业部,O=anywide,L=DALIAN,ST=LIAONING,c=CN" -storepass suxuan696@gmail.com -keypass jackson.song
	keytool -export -v -alias srchen -keystore dawdler.keystore -storepass suxuan696@gmail.com -rfc -file dawdler.cer
 */
	
	public CertificateOperator(String keyStorePath,String alias,String password) {
		this.alias = alias;
		this.password=password.toCharArray();
		this.keyStorePath = keyStorePath;
	}
	public CertificateOperator(String certificatePath) {
		this.certificatePath = certificatePath;
	}
	private String alias;
	private char[] password;
	private String keyStorePath;
	private String certificatePath;
	public static enum KeyStoreConfig {

		JCEKS("jceks"), JKS("jks"), DKS("dks"), PKCS11("pkcs11"), PKCS12("pkcs12");
		private String name;

		private KeyStoreConfig(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}

	public final String X509 = "X.509";

	private PrivateKey getPrivateKey(KeyStoreConfig keyStore)
			throws Exception {
		KeyStore ks = getKeyStore(keyStore);
		PrivateKey key = (PrivateKey) ks.getKey(alias, password);
		return key;
	}

	private PublicKey getPublicKey() throws Exception {
		Certificate certificate = getCertificate();
		PublicKey key = certificate.getPublicKey();
		return key;
	}

	private Certificate getCertificate() throws Exception {
		CertificateFactory certificateFactory = CertificateFactory.getInstance(X509);
		FileInputStream in = new FileInputStream(certificatePath);
		Certificate certificate = certificateFactory.generateCertificate(in);
		in.close();
		return certificate;
	}

	private Certificate getCertificate( KeyStoreConfig keyStore)
			throws Exception {
		KeyStore ks = getKeyStore(keyStore);
		return getCertificate(ks);
	}

	private Certificate getCertificate(KeyStore keyStore) throws Exception {
		Certificate certificate = keyStore.getCertificate(alias);
		return certificate;
	}

	public KeyStore getKeyStore(KeyStoreConfig keyStore) throws Exception {
		KeyStore store = null;
		FileInputStream is = new FileInputStream(keyStorePath);
		store = getKeyStore(is, keyStore);
		is.close();
		return store;
	}

	public KeyStore getKeyStore(InputStream in, KeyStoreConfig keyStore) throws Exception {
		KeyStore ks = KeyStore.getInstance(keyStore.getName());
		ks.load(in, password);
		return ks;
	}

	public byte[] encrypt(byte[] data, KeyStoreConfig keyStore)
			throws Exception {
		if(privateEncryptCipher != null) 
			return privateEncryptCipher.doFinal(data);
		PrivateKey privateKey = getPrivateKey(keyStore);
		return encrypt(data, privateKey);

	}
	private Cipher privateEncryptCipher = null;
	private byte[] encrypt(byte[] data, PrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);
		privateEncryptCipher = cipher;
		return cipher.doFinal(data);
	}

	private Cipher publicEncryptCipher = null; 
	public byte[] encrypt(byte[] data) throws Exception {
		if(publicEncryptCipher != null)
			return publicEncryptCipher.doFinal(data);
		PublicKey publicKey = getPublicKey();
		return encrypt(data, publicKey);

	}

	private byte[] encrypt(byte[] data, PublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		publicEncryptCipher = cipher;
		return cipher.doFinal(data);

	}

	private Cipher privateDecryptCipher = null;
	public byte[] decrypt(byte[] data,KeyStoreConfig keyStore)
			throws Exception {
		if(privateDecryptCipher!=null)
			return privateDecryptCipher.doFinal(data);
		PrivateKey privateKey = getPrivateKey(keyStore);
		return decrypt(data, privateKey);
	}

	private byte[] decrypt(byte[] data, PrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		privateDecryptCipher = cipher;
		return cipher.doFinal(data);
	}

	public byte[] decrypt(byte[] data) throws Exception {
		if(publicDecryptCipher != null)
			return publicDecryptCipher.doFinal(data);
		PublicKey publicKey = getPublicKey();
		return decrypt(data, publicKey);  
	}
	private Cipher publicDecryptCipher = null;
	private byte[] decrypt(byte[] data, PublicKey publicKey) throws Exception {
		Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
		cipher.init(Cipher.DECRYPT_MODE, publicKey);
		publicDecryptCipher = cipher;
		return cipher.doFinal(data);

	}

	public boolean verifyCertificate() {
		return verifyCertificate(new Date());
	}

	public boolean verifyCertificate(Date date) {
		boolean status = true;
		try {
			Certificate certificate = getCertificate();
			status = verifyCertificate(date, certificate);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	private boolean verifyCertificate(Date date, Certificate certificate) {
		boolean status = true;
		try {
			X509Certificate x509Certificate = (X509Certificate) certificate;
			x509Certificate.checkValidity(date);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	public byte[] sign(byte[] data, KeyStoreConfig keyStore)
			throws Exception {
		Certificate certificate = getCertificate(keyStore);
		PrivateKey privateKey = getPrivateKey(keyStore);
		return sign(data, certificate, privateKey);
	}

	public byte[] sign(byte[] data, Certificate certificate, PrivateKey privateKey) throws Exception {
		X509Certificate x509Certificate = (X509Certificate) certificate;
		Signature signature = Signature.getInstance(x509Certificate.getSigAlgName());
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	public boolean verify(byte[] data, byte[] sign) throws Exception {
		Certificate certificate = getCertificate();
		return verify(data, sign, certificate);
	}

	public boolean verify(byte[] data, byte[] sign, Certificate certificate) throws Exception {
		X509Certificate x509Certificate = (X509Certificate) certificate;
		PublicKey publicKey = x509Certificate.getPublicKey();
		Signature signature = Signature.getInstance(x509Certificate.getSigAlgName());
		signature.initVerify(publicKey);
		signature.update(data);

		return signature.verify(sign);
	}

	public boolean verifyCertificate(Date date,
			KeyStoreConfig keyStore) {
		boolean status = true;
		try {
			Certificate certificate = getCertificate(keyStore);
			status = verifyCertificate(date, certificate);
		} catch (Exception e) {
			status = false;
		}
		return status;
	}

	public boolean verifyCertificate(KeyStoreConfig keyStore) {
		return verifyCertificate(new Date(), keyStore);
	}
//	public static void main(String[] args) throws Exception {
//		String certificatePath = "/Users/jackson.song/Desktop/key/dawdler.cer";
//		String keystore = "/Users/jackson.song/Desktop/key/dawdler.keystore";
//		CertificateOperator publicC = new CertificateOperator(certificatePath);
//		CertificateOperator publicP = new CertificateOperator(keystore,"www.dawdler.com","123456");
//		byte[] bs = publicC.encrypt("shff".getBytes());
//		System.out.println(bs);
////		bs = CertificateUtils.getInstance().decrypt(bs, publicPath);
////		System.out.println(bs);
//		
//		bs = publicP.decrypt(bs, KeyStoreConfig.DKS);
//		String s = new String(bs);
//		System.out.println(s);
//		long t1 = System.currentTimeMillis();
//		for(int i =0;i<1;i++) {
////			bs = CertificateUtils.getInstance().encrypt("hello wordhello wordhello wordhello wordhello wordhello wordhello wordhello wordhello word".getBytes(), keystore,"www.dawdler.com", "123456".toCharArray(),KeyStoreConfig.DKS);
////			 bs = CertificateUtils.getInstance().decrypt(bs,publicPath);
//			bs = publicP.encrypt("hello wordhello wordhello wordhello wordhello wordhe".getBytes(),KeyStoreConfig.DKS);
//			bs = publicC.decrypt(bs);
//		}
//		s = new String(bs);
//		
////		bs = CertificateUtils.getInstance().encrypt("hello wordhello wordhello wordhello wordhello wordhello wordhello wordhello wordhello word".getBytes(), keystore,"www.dawdler.com", "123456".toCharArray(),KeyStoreConfig.DKS);
////		 bs = CertificateUtils.getInstance().decrypt(bs,publicPath);
//		System.out.println(bs);
////		bs = CertificateUtils.getInstance().decrypt(bs, publicPath);
////		System.out.println(bs);
//		long t2 = System.currentTimeMillis();
//		System.out.println(t2-t1);
//		System.out.println(s);
//		
//		
//		
//	}
}
