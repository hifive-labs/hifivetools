/*
 * Copyright (C) 2012 NS Solutions Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
 package jp.co.nssol.h5.tool.jslint.engine.option;

/**
 * オプションファイルのラッパーインターフェース.
 * 
 * @author NS Solutions Corporation
 * 
 */
public interface CheckOptionFileWrapper {

	/**
	 * キーとエンジンからオプションを取得する.
	 * 
	 * 
	 * @param key キー.
	 * @param engine エンジン
	 * @return オプション.
	 */
	public abstract CheckOption getOption(String key, String engine);

	/**
	 * 全てのオプションを取得する.
	 * 
	 * @param engine チェッカエンジン.
	 * @return オプション.
	 */
	public abstract CheckOption[] getOptions(Engine engine);

	/**
	 * 
	 * 有効状態のオプションを取得する.
	 * 
	 * @param engine チェッカエンジン.
	 * @return 有効状態のオプション.
	 */
	public abstract CheckOption[] getEnableOptions(Engine engine);

	/**
	 * オプションを保存する.
	 * 
	 */
	public abstract void saveOption();

	/**
	 * オプションを追加する.
	 * 
	 * @param option オプション.
	 */
	public abstract void addOption(CheckOption option);

	/**
	 * 
	 * キーが同じオプションを更新する.
	 * 
	 * @param option オプション.
	 */
	public abstract void updateOption(CheckOption option);

}